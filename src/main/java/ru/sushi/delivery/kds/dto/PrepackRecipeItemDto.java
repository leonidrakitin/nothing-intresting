package ru.sushi.delivery.kds.dto;

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
    private final Double initAmount;
    private final Double finalAmount;
    private final Double lossesAmount;
    private final Double lossesPercentage;

    public static PrepackRecipeItemDto of(String name, PrepackRecipe prepackRecipe) {
        return PrepackRecipeItemDto.builder()
                .sourceId(prepackRecipe.getSourceId())
                .sourceType(prepackRecipe.getSourceType())
                .name(name)
                .initAmount(prepackRecipe.getInitAmount())
                .finalAmount(prepackRecipe.getFinalAmount())
                .lossesAmount(prepackRecipe.getLossesAmount())
                .lossesPercentage(prepackRecipe.getLossesPercentage())
                .build();
    }
}
