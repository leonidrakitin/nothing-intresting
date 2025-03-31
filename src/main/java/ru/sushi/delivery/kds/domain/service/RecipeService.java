package ru.sushi.delivery.kds.domain.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.controller.dto.AbstractProductData;
import ru.sushi.delivery.kds.domain.controller.dto.MeasurementUnitDto;
import ru.sushi.delivery.kds.domain.controller.dto.MenuItemRecipeDto;
import ru.sushi.delivery.kds.domain.controller.dto.PrepackRecipeData;
import ru.sushi.delivery.kds.domain.controller.dto.SourceDto;
import ru.sushi.delivery.kds.domain.persist.entity.Measurement;
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
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeService {

    private final IngredientItemService ingredientItemService;
    private final MenuItemRecipeRepository menuItemRecipeRepository;
    private final PrepackRecipeRepository prepackRecipeRepository;
    private final PrepackItemService prepackItemService;
    private final SourceItemService sourceItemService;
    private final SourceService sourceService;
    private final PrepackService prepackService;
    private final MenuItemService menuItemService;
    private final MeasurementService measurementService;

    public List<PrepackRecipeItemDto> getPrepackRecipe(Long prepackId) {
        return this.prepackRecipeRepository.findByPrepackId(prepackId).stream()
                .map(recipe -> PrepackRecipeItemDto.of(
                        this.sourceService.getSourceName(recipe.getSourceId(), recipe.getSourceType()),
                        recipe
                ))
                .toList();
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
                        this.sourceItemService.getSourceItemName(item)
                ));
            }
            else {
                item.setDiscontinuedComment(String.format(
                        "%s '%s' автоматически был списан в минус системой",
                        item.getSourceType().getValue(),
                        this.sourceItemService.getSourceItemName(item)
                ));
                item.setDiscontinuedComment("Закончился при приготовлении  (авто)");
            }
            log.info(item.getDiscontinuedComment());
        }
        return calAmount > 0 ? calAmount : spentAmount - item.getAmount();
    }

    @Transactional
    public void writeOffSourceItems(double spentAmount, Long sourceId, SourceType sourceType) {
        Iterator<SourceItem> itemIterator = this.sourceItemService.getSourceActiveItems(sourceId, sourceType).iterator();
        while (spentAmount > 0) {
            if (!itemIterator.hasNext()) {
                log.error(
                        "Unexpected behaviour spent amount was not write-off, needed {}, sourceId {}, sourceType {}",
                        spentAmount,
                        sourceId,
                        sourceType
                );
                return;
            }
            else {
                spentAmount = this.writeOffFinishedItem(itemIterator.next(), spentAmount, itemIterator.hasNext());
            }
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
                        this.sourceService.getSourceName(
                                prepackRecipe.getSourceId(),
                                prepackRecipe.getSourceType()),
                        this.sourceService.getSource(
                                prepackRecipe.getSourceId(),
                                prepackRecipe.getSourceType())
                )
        ).toList();
    }

    public void savePrepackRecipe(PrepackRecipeData prepackRecipeData, SourceDto sourceDto, Long prepackId) {
        Prepack prepack = this.prepackService.get(prepackId);

        Measurement measurement = Optional.ofNullable(prepackRecipeData.getMeasurementUnit())
                .map(MeasurementUnitDto::getId)
                .map(this.measurementService::getById)
                .orElseGet(() -> this.sourceService.getSourceMeasurementUnit(
                        sourceDto.getId(), SourceType.valueOf(sourceDto.getType())
                ));

        PrepackRecipe prepackRecipe = Optional.ofNullable(prepackRecipeData.getId())
                .map(this.prepackRecipeRepository::findById)
                .flatMap(Function.identity())
                .map(p -> setNewPrepackRecipeData(p, prepackRecipeData, sourceDto, prepack, measurement))
                .orElseGet(() -> PrepackRecipe.of(prepackRecipeData, sourceDto, prepack, measurement));

        this.prepackRecipeRepository.save(prepackRecipe);
    }

    public PrepackRecipe setNewPrepackRecipeData(
            PrepackRecipe prepackRecipe,
            PrepackRecipeData prepackRecipeData,
            SourceDto sourceDto,
            Prepack prepack,
            Measurement measurement
    ) {
        return prepackRecipe.toBuilder()
                .id(prepackRecipeData.getId())
                .prepack(prepack)
                .sourceId(sourceDto.getId())
                .measurement(measurement)
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
                                this.sourceService.getSourceName(
                                        menuItemRecipe.getSourceId(),
                                        menuItemRecipe.getSourceType()),
                                this.sourceService.getSource(
                                        menuItemRecipe.getSourceId(),
                                        menuItemRecipe.getSourceType())
                        )
                )
                .toList();
    }

    public void saveMenuRecipe(MenuItemRecipeDto menuItemRecipeDto, SourceDto sourceDto, Long menuId) {
        MenuItem menuItem = this.menuItemService.getMenuItemById(menuId);

        Measurement measurement = Optional.ofNullable(menuItemRecipeDto.getMeasurementUnit())
                .map(MeasurementUnitDto::getId)
                .map(this.measurementService::getById)
                .orElseGet(() -> this.sourceService.getSourceMeasurementUnit(
                        sourceDto.getId(), SourceType.valueOf(sourceDto.getType())
                ));

        MenuItemRecipe menuItemRecipe = Optional.ofNullable(menuItemRecipeDto.getId())
                .map(this.menuItemRecipeRepository::findById)
                .flatMap(Function.identity())
                .map(m -> setNewMenuItemRecipeData(m, menuItemRecipeDto, sourceDto, menuItem, measurement))
                .orElseGet(() -> MenuItemRecipe.of(menuItemRecipeDto, sourceDto, menuItem, measurement));

        this.menuItemRecipeRepository.save(menuItemRecipe);
    }

    public MenuItemRecipe setNewMenuItemRecipeData(
            MenuItemRecipe menuItemRecipe,
            MenuItemRecipeDto menuItemRecipeDto,
            SourceDto sourceDto,
            MenuItem menuItem,
            Measurement measurement
    ) {
        return menuItemRecipe.toBuilder()
                .id(menuItemRecipe.getId())
                .menuItem(menuItem)
                .stationId(menuItemRecipeDto.getStationId())
                .measurement(measurement)
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

    //todo Можно сделать с wrapper
    public List<Recipe> checkRecipeDependencies(AbstractProductData productData, SourceType sourceType) {
        List<MenuItemRecipe> menuItemRecipes = menuItemRecipeRepository.findAllBySourceIdAndSourceType(
                productData.getId(),
                sourceType
        );

        List<PrepackRecipe> prepackRecipes = prepackRecipeRepository.findAllBySourceIdAndSourceType(
                productData.getId(),
                sourceType
        );

        return Stream.concat(menuItemRecipes.stream(), prepackRecipes.stream()).toList();
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
            SourceItem sourceItem = this.sourceItemService.getSourceActiveItems(recipe.getSourceId(), recipe.getSourceType())
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
            default -> throw new IllegalStateException("Unexpected value: " + sourceItem.getSourceType());
        };
    }
}
