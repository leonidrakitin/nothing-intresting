package ru.sushi.delivery.kds.domain.controller.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.sushi.delivery.kds.domain.persist.entity.recipe.PrepackRecipe;

@Builder
@Getter
@RequiredArgsConstructor
public class PrepackRecipeItemDto {

    private final Long id;
    private final Long sourceId;
    private final String sourceType;
    private final String name;
    private final Double initAmount; // первая колонка
    private final Double finalAmount; //выход
    private final Double lossesAmount; //потери
    private final Double lossesPercentage; //потери в %

    public static PrepackRecipeItemDto of(PrepackRecipe prepackRecipe) {
        return PrepackRecipeItemDto.builder()
                .id(prepackRecipe.getId())
                .sourceId(prepackRecipe.getSourceId())
                .sourceType(prepackRecipe.getSourceType().name())
                .name(prepackRecipe.getPrepack().getName())
                .initAmount(prepackRecipe.getInitAmount())
                .finalAmount(prepackRecipe.getFinalAmount())
                .lossesAmount(prepackRecipe.getLossesAmount())
                .lossesPercentage(prepackRecipe.getLossesPercentage())
                .build();
    }
}
