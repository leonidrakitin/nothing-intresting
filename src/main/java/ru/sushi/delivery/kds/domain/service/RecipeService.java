package ru.sushi.delivery.kds.domain.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.persist.entity.product.IngredientItem;
import ru.sushi.delivery.kds.domain.persist.entity.product.PrepackItem;
import ru.sushi.delivery.kds.domain.persist.entity.product.SourceItem;
import ru.sushi.delivery.kds.domain.persist.entity.recipe.Recipe;
import ru.sushi.delivery.kds.domain.persist.repository.product.IngredientItemRepository;
import ru.sushi.delivery.kds.domain.persist.repository.product.PrepackItemRepository;
import ru.sushi.delivery.kds.domain.persist.repository.recipe.PositionRecipeRepository;
import ru.sushi.delivery.kds.domain.persist.repository.recipe.PrepackRecipeRepository;
import ru.sushi.delivery.kds.dto.PrepackRecipeItemDto;
import ru.sushi.delivery.kds.model.DiscontinuedReason;
import ru.sushi.delivery.kds.model.SourceType;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeService {

    private final IngredientItemRepository ingredientItemRepository;
    private final IngredientItemService ingredientItemService;
    private final PositionRecipeRepository positionRecipeRepository;
    private final PrepackRecipeRepository prepackRecipeRepository;
    private final PrepackItemRepository prepackItemRepository;
    private final PrepackItemService prepackItemService;

    public List<SourceItem> getSourceItems(Long sourceId, SourceType sourceType) {
        return switch (sourceType) {
            case INGREDIENT -> this.ingredientItemRepository.findActiveByIngredientId(sourceId);
            case PREPACK -> this.prepackItemRepository.findActiveByPrepackId(sourceId);
        };
    }

    public List<PrepackRecipeItemDto> getPrepackRecipe(Long prepackId) {
        return this.prepackRecipeRepository.findByPrepackId(prepackId).stream()
                .map(PrepackRecipeItemDto::of)
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
                this.getSourceItemName(sourceItem),
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
                this.getSourceItemName(sourceItem),
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
                        this.getSourceItemName(item)
                ));
            } else {
                item.setDiscontinuedComment(String.format(
                        "%s '%s' автоматически был списан в минус системой",
                        item.getSourceType().getValue(),
                        this.getSourceItemName(item)
                ));
                item.setDiscontinuedComment("Закончился при приготовлении  (авто)");
            }
            log.info(item.getDiscontinuedComment());
        }
        return calAmount > 0 ? calAmount : spentAmount - item.getAmount();
    }

    @Transactional
    public void calculatePositionsBalance(List<Long> positionIds) {
        List<Recipe> positionRecipes = this.positionRecipeRepository.findByPositionIds(positionIds);
        positionRecipes.forEach(this::calculateRecipe);
        this.checkAndNotifyIfAlmostFinished(positionRecipes);
    }

    private void checkAndNotifyIfAlmostFinished(List<Recipe> positionRecipes) {
        Set<SourceItem> checkedSourceItems = new HashSet<>();
        ListIterator<Recipe> recipeIterator = positionRecipes.listIterator();
        while (recipeIterator.hasNext()) {
            Recipe recipe = recipeIterator.next();
            if (recipe.getSourceType() == SourceType.PREPACK) {
                this.prepackRecipeRepository.findByPrepackId(recipe.getSourceId())
                        .forEach(recipeIterator::add);
            }
            SourceItem sourceItem = this.getSourceItems(recipe.getSourceId(), recipe.getSourceType()).getFirst();
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
        ListIterator<SourceItem> sourceItems = this.getSourceItems(recipe.getSourceId(), recipe.getSourceType())
                .listIterator();

        while (spentAmount > 0) {
            if (!sourceItems.hasNext()) {
                log.error("Unexpected behaviour recipe spent amount was not write-off");
                return;
            }
            spentAmount = this.writeOffFinishedItem(sourceItems.next(), spentAmount, sourceItems.hasNext());
        }
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

    private String getSourceItemName(SourceItem sourceItem) {
        return switch (sourceItem.getSourceType()) {
            case INGREDIENT -> ((IngredientItem) sourceItem).getIngredient().getName();
            case PREPACK -> ((PrepackItem) sourceItem).getPrepack().getName();
        };
    }
}
