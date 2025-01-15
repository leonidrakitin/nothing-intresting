package ru.sushi.delivery.kds.domain.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.controller.dto.MenuItemRecipeDto;
import ru.sushi.delivery.kds.domain.controller.dto.PrepackRecipeData;
import ru.sushi.delivery.kds.domain.controller.dto.SourceDto;
import ru.sushi.delivery.kds.domain.persist.entity.product.IngredientItem;
import ru.sushi.delivery.kds.domain.persist.entity.product.MenuItem;
import ru.sushi.delivery.kds.domain.persist.entity.product.Prepack;
import ru.sushi.delivery.kds.domain.persist.entity.product.PrepackItem;
import ru.sushi.delivery.kds.domain.persist.entity.product.SourceItem;
import ru.sushi.delivery.kds.domain.persist.entity.recipe.MenuItemRecipe;
import ru.sushi.delivery.kds.domain.persist.entity.recipe.PrepackRecipe;
import ru.sushi.delivery.kds.domain.persist.entity.recipe.Recipe;
import ru.sushi.delivery.kds.domain.persist.repository.recipe.MenuItemRecipeRepository;
import ru.sushi.delivery.kds.domain.persist.repository.recipe.PrepackRecipeRepository;
import ru.sushi.delivery.kds.dto.PrepackRecipeItemDto;
import ru.sushi.delivery.kds.model.DiscontinuedReason;
import ru.sushi.delivery.kds.model.SourceType;

import java.time.Instant;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeService {

    private final IngredientItemService ingredientItemService;
    private final MenuItemRecipeRepository menuItemRecipeRepository;
    private final PrepackRecipeRepository prepackRecipeRepository;
    private final PrepackItemService prepackItemService;
    private final SourceService sourceService;
    private final PrepackService prepackService;
    private final MenuItemService menuItemService;

    public List<PrepackRecipeItemDto> getPrepackRecipe(Long prepackId) {
        return this.prepackRecipeRepository.findByPrepackId(prepackId).stream()
                .map(recipe -> PrepackRecipeItemDto.of(
                        this.sourceService.getSourceItemName(recipe.getSourceId(), recipe.getSourceType()),
                        recipe
                ))
                .toList();
    }

    public void writeOffSpoiledItem(String employeeName, SourceItem sourceItem) {
        if (sourceItem.getExpirationDate().isAfter(Instant.now())) {
            throw new IllegalArgumentException("Невозможно списать продукт");
        }
        sourceItem.setDiscontinuedAt(Instant.now());
        sourceItem.setDiscontinuedReason(DiscontinuedReason.SPOILED);
        sourceItem.setDiscontinuedComment(String.format(
                "%s '%s' был списан сотрудником %s по причине 'продукт испорчен'",
                sourceItem.getSourceType().getValue(),
                this.sourceService.getSourceItemName(sourceItem),
                employeeName
        ));
        log.info(sourceItem.getDiscontinuedComment());
    }

    public void writeOffItemWithOtherReason(String employeeName, String reason, SourceItem sourceItem) {
        sourceItem.setDiscontinuedAt(Instant.now());
        sourceItem.setDiscontinuedReason(DiscontinuedReason.SPOILED);
        sourceItem.setDiscontinuedComment(String.format(
                "%s '%s' был списан сотрудником %s по причине %s",
                sourceItem.getSourceType().getValue(),
                this.sourceService.getSourceItemName(sourceItem),
                employeeName,
                reason
        ));
        log.info(sourceItem.getDiscontinuedComment());
    }

    public double writeOffFinishedItem(SourceItem item, double spentAmount, boolean last) {
        double calAmount = item.getAmount() - spentAmount;
        item.setAmount(calAmount >= 0 || last ? calAmount : 0);
        if (item.getAmount() <= 0) {
            item.setDiscontinuedReason(DiscontinuedReason.FINISHED);
            item.setDiscontinuedAt(Instant.now());
            if (item.getAmount() == 0) {
                item.setDiscontinuedComment(String.format(
                        "%s '%s' автоматически был списан системой",
                        item.getSourceType().getValue(),
                        this.sourceService.getSourceItemName(item)
                ));
            }
            else {
                item.setDiscontinuedComment(String.format(
                        "%s '%s' автоматически был списан в минус системой",
                        item.getSourceType().getValue(),
                        this.sourceService.getSourceItemName(item)
                ));
                item.setDiscontinuedComment("Закончился при приготовлении  (авто)");
            }
            log.info(item.getDiscontinuedComment());
        }
        return calAmount > 0 ? calAmount : spentAmount - item.getAmount();
    }

    @Transactional
    public void writeOffSourceItems(double spentAmount, Long sourceId, SourceType sourceType) {
        Iterator<SourceItem> itemIterator = this.sourceService.getSourceActiveItems(sourceId, sourceType).iterator();
        while (spentAmount > 0) {
            if (!itemIterator.hasNext()) {
                throw new IllegalArgumentException(String.format(
                        "Unexpected behaviour spent amount was not write-off, needed %f, sourceId %d, sourceType %s",
                        spentAmount,
                        sourceId,
                        sourceType
                ));
            }
            spentAmount = this.writeOffFinishedItem(itemIterator.next(), spentAmount, itemIterator.hasNext());
        }
    }

    @Transactional
    public void calculateMenuItemsBalance(List<Long> menuItemIds) {
        List<Recipe> menuItemRecipes = this.menuItemRecipeRepository.findByMenuItemIds(menuItemIds);
        menuItemRecipes.forEach(this::calculateRecipe);
        this.checkAndNotifyIfAlmostFinished(menuItemRecipes);
    }

    public List<PrepackRecipeData> getPrepackRecipeByPrepackId(Long prepackId) {
        List<PrepackRecipe> prepackRecipes = this.prepackRecipeRepository.findByPrepackId(prepackId);
        return prepackRecipes.stream().map(prepackRecipe -> PrepackRecipeData.of(
                        prepackRecipe,
                        this.sourceService.getSourceItemName(
                                prepackRecipe.getSourceId(),
                                prepackRecipe.getSourceType())
                )
        ).toList();
    }

    public void savePrepackRecipe(PrepackRecipeData prepackRecipeData, SourceDto sourceDto, Long prepackId) {
        Prepack prepack = this.prepackService.get(prepackId);

        PrepackRecipe prepackRecipe = Optional.ofNullable(prepackRecipeData.getId())
                .map(this.prepackRecipeRepository::findById)
                .flatMap(Function.identity())
                .map(p -> setNewPrepackRecipeData(p, prepackRecipeData, sourceDto, prepack))
                .orElseGet(() -> PrepackRecipe.of(prepackRecipeData, sourceDto, prepack));

        this.prepackRecipeRepository.save(prepackRecipe);
    }

    public PrepackRecipe setNewPrepackRecipeData(
            PrepackRecipe prepackRecipe,
            PrepackRecipeData prepackRecipeData,
            SourceDto sourceDto,
            Prepack prepack
    ) {
        return prepackRecipe.toBuilder()
                .id(prepackRecipeData.getId())
                .prepack(prepack)
                .sourceId(sourceDto.getId())
                .sourceType(SourceType.valueOf(sourceDto.getType()))
                .initAmount(prepackRecipeData.getInitAmount())
                .finalAmount(prepackRecipeData.getFinalAmount())
                .lossesAmount(prepackRecipeData.getLossesAmount())
                .lossesPercentage(prepackRecipeData.getLossesPercentage())
                .build();
    }

    public void deletePrepackRecipe(PrepackRecipeData prepackRecipeData) {
        this.prepackRecipeRepository.deleteById(prepackRecipeData.getId());
    }

    public List<MenuItemRecipeDto> getMenuRecipeByMenuId(Long menuId) {
        List<MenuItemRecipe> menuItemRecipes = this.menuItemRecipeRepository.findByMenuItemId(menuId);
        return menuItemRecipes.stream().map(menuItemRecipe -> MenuItemRecipeDto.of(
                        menuItemRecipe,
                        this.sourceService.getSourceItemName(
                                menuItemRecipe.getSourceId(),
                                menuItemRecipe.getSourceType())
                )
        ).toList();
    }

    public void saveMenuRecipe(MenuItemRecipeDto menuItemRecipeDto, SourceDto sourceDto, Long menuId) {
        MenuItem menuItem = this.menuItemService.getMenuItemById(menuId);

        MenuItemRecipe menuItemRecipe = Optional.ofNullable(menuItemRecipeDto.getId())
                .map(this.menuItemRecipeRepository::findById)
                .flatMap(Function.identity())
                .map(m -> setNewMenuItemRecipeData(m, menuItemRecipeDto, sourceDto, menuItem))
                .orElseGet(() -> MenuItemRecipe.of(menuItemRecipeDto, sourceDto, menuItem));

        this.menuItemRecipeRepository.save(menuItemRecipe);
    }

    public MenuItemRecipe setNewMenuItemRecipeData(
            MenuItemRecipe menuItemRecipe,
            MenuItemRecipeDto menuItemRecipeDto,
            SourceDto sourceDto,
            MenuItem menuItem
    ) {
        return menuItemRecipe.toBuilder()
                .id(menuItemRecipe.getId())
                .menuItem(menuItem)
                .sourceId(sourceDto.getId())
                .sourceType(SourceType.valueOf(sourceDto.getType()))
                .initAmount(menuItemRecipeDto.getInitAmount())
                .finalAmount(menuItemRecipeDto.getFinalAmount())
                .lossesAmount(menuItemRecipeDto.getLossesAmount())
                .lossesPercentage(menuItemRecipeDto.getLossesPercentage())
                .build();
    }

    public void deleteMenuItemRecipe(MenuItemRecipeDto menuItemRecipeDto) {
        this.menuItemRecipeRepository.deleteById(menuItemRecipeDto.getId());
    }

    private void checkAndNotifyIfAlmostFinished(List<Recipe> menuItemRecipes) {
        Set<SourceItem> checkedSourceItems = new HashSet<>();
        ListIterator<Recipe> recipeIterator = menuItemRecipes.listIterator();
        while (recipeIterator.hasNext()) {
            Recipe recipe = recipeIterator.next();
            if (recipe.getSourceType() == SourceType.PREPACK) {
                this.prepackRecipeRepository.findByPrepackId(recipe.getSourceId())
                        .forEach(recipeIterator::add);
            }
            SourceItem sourceItem = this.sourceService.getSourceActiveItems(recipe.getSourceId(), recipe.getSourceType())
                    .getFirst();
            if (checkedSourceItems.contains(sourceItem)) {
                continue;
            }
            checkedSourceItems.add(sourceItem);
            if (this.checkIfAlmostFinished(sourceItem)) {
                log.info("Almost finished"); // todo notify web socket, notifications, notify telegram
            }
        }
    }

    private void calculateRecipe(Recipe recipe) {
        double spentAmount = recipe.getInitAmount();
        this.writeOffSourceItems(spentAmount, recipe.getSourceId(), recipe.getSourceType());
    }

    private boolean checkIfAlmostFinished(SourceItem sourceItem) {
        return switch (sourceItem.getSourceType()) {
            case INGREDIENT -> this.ingredientItemService.checkIfAlmostEmpty(
                    ((IngredientItem) sourceItem).getIngredient()
            );
            case PREPACK -> this.prepackItemService.checkIfAlmostEmpty(
                    ((PrepackItem) sourceItem).getPrepack()
            );
        };
    }
}
