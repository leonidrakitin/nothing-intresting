package ru.sushi.delivery.kds.domain.controller.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.sushi.delivery.kds.domain.persist.entity.recipe.PrepackRecipe;
import ru.sushi.delivery.kds.model.SourceType;

@Builder
@Getter
@RequiredArgsConstructor
public class PrepackRecipeItemDto {
    private final Long sourceId;
    private final SourceType sourceType;
    private final String name;
    private final Double initAmount; // первая колонка
    private final Double finalAmount; //выход
    private final Double lossesAmount; //потери
    private final Double lossesPercentage; //потери в %

    public static PrepackRecipeItemDto of(PrepackRecipe prepackRecipe) {
        return PrepackRecipeItemDto.builder()
                .sourceId(prepackRecipe.getSourceId())
                .sourceType(prepackRecipe.getSourceType())
                .name(prepackRecipe.getPrepack().getName())
                .initAmount(prepackRecipe.getInitAmount())
                .finalAmount(prepackRecipe.getFinalAmount())
                .lossesAmount(prepackRecipe.getLossesAmount())
                .lossesPercentage(prepackRecipe.getLossesPercentage())
                .build();
    }
}
