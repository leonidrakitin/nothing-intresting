package ru.sushi.delivery.kds.domain.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sushi.delivery.kds.domain.persist.entity.recipe.PrepackRecipe;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrepackRecipeDto {

    private Long id;
    private String sourceName;
    private Double initAmount;
    private Double finalAmount;
    private Double lossesAmount;
    private Double lossesPercentage;

    public static PrepackRecipeDto of(PrepackRecipe prepackRecipe, String sourceName) {
        return PrepackRecipeDto.builder()
                .id(prepackRecipe.getId())
                .sourceName(sourceName)
                .initAmount(prepackRecipe.getInitAmount())
                .finalAmount(prepackRecipe.getFinalAmount())
                .lossesAmount(prepackRecipe.getLossesAmount())
                .lossesPercentage(prepackRecipe.getLossesPercentage())
                .build();
    }
}
