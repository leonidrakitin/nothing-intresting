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
public class PrepackRecipeData {

    private Long id;
    private String sourceName;
    private MeasurementUnitDto measurementUnit;
    private Double initAmount;
    private Double finalAmount;
    private Double lossesAmount;
    private Double lossesPercentage;

    public static PrepackRecipeData of(PrepackRecipe prepackRecipe, String sourceName) {
        return PrepackRecipeData.builder()
                .id(prepackRecipe.getId())
                .sourceName(sourceName)
                .measurementUnit(new MeasurementUnitDto(
                        prepackRecipe.getMeasurement().getId(),
                        prepackRecipe.getMeasurement().getName()
                ))
                .initAmount(prepackRecipe.getInitAmount())
                .finalAmount(prepackRecipe.getFinalAmount())
                .lossesAmount(prepackRecipe.getLossesAmount())
                .lossesPercentage(prepackRecipe.getLossesPercentage())
                .build();
    }
}
