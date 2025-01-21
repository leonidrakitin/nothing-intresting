package ru.sushi.delivery.kds.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.controller.dto.request.WriteOffRequest;
import ru.sushi.delivery.kds.domain.persist.entity.product.IngredientItem;
import ru.sushi.delivery.kds.domain.persist.entity.product.PrepackItem;
import ru.sushi.delivery.kds.model.DiscontinuedReason;
import ru.sushi.delivery.kds.model.SourceType;

@Service
@RequiredArgsConstructor
public class WriteOffService {

    private final RecipeService recipeService;
    private final IngredientItemService ingredientItemService;
    private final PrepackItemService prepackItemService;

    public void writeOff(WriteOffRequest writeOffRequest) {
        if (writeOffRequest.getSourceType() == SourceType.INGREDIENT) {

            IngredientItem ingredientItem = ingredientItemService.get(writeOffRequest.getSourceItemId());
            ingredientItem.setAmount(Math.max(0, ingredientItem.getAmount() - writeOffRequest.getWriteOffAmount()));


            if (writeOffRequest.getDiscontinuedReason() == DiscontinuedReason.OTHER) {
                recipeService.writeOffItemWithOtherReason(
                        writeOffRequest.getEmployeeName(),
                        writeOffRequest.getCustomReasonComment(),
                        ingredientItem
                );
            }
            else if (writeOffRequest.getDiscontinuedReason() == DiscontinuedReason.SPOILED) {
                recipeService.writeOffSpoiledItem(
                        writeOffRequest.getEmployeeName(),
                        ingredientItem
                );
            }
        }
        else if (writeOffRequest.getSourceType() == SourceType.PREPACK) {
            PrepackItem prepackItem = prepackItemService.get(writeOffRequest.getSourceItemId());
            prepackItem.setAmount(Math.max(0, prepackItem.getAmount() - writeOffRequest.getWriteOffAmount()));


            if (writeOffRequest.getDiscontinuedReason() == DiscontinuedReason.OTHER) {
                recipeService.writeOffItemWithOtherReason(
                        writeOffRequest.getEmployeeName(),
                        writeOffRequest.getCustomReasonComment(),
                        prepackItem
                );
            }
            else if (writeOffRequest.getDiscontinuedReason() == DiscontinuedReason.SPOILED) {
                recipeService.writeOffSpoiledItem(
                        writeOffRequest.getEmployeeName(),
                        prepackItem
                );
            }
        }
    }
}

