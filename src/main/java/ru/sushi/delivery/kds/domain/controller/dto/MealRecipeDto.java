package ru.sushi.delivery.kds.domain.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sushi.delivery.kds.domain.persist.entity.recipe.MealRecipe;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealRecipeDto {

    private Long id;
    private String sourceName;
    private MeasurementUnitDto measurementUnit;
    private Long stationId;
    private Double initAmount;
    private Double finalAmount;
    private Double lossesAmount;
    private Double lossesPercentage;
    private Long priority;

    public static MealRecipeDto of(MealRecipe mealRecipe, String sourceName) {
        return MealRecipeDto.builder()
                .id(mealRecipe.getId())
                .sourceName(sourceName) //todo everywhere except kitchen String.format("%s [%s]", sourceName, mealRecipe.getSourceType().name())
                .measurementUnit(new MeasurementUnitDto(
                        mealRecipe.getMeasurement().getId(),
                        mealRecipe.getMeasurement().getName()
                ))
                .stationId(mealRecipe.getStationId())
                .initAmount(mealRecipe.getInitAmount())
                .finalAmount(mealRecipe.getFinalAmount())
                .lossesAmount(mealRecipe.getLossesAmount())
                .lossesPercentage(mealRecipe.getLossesPercentage())
                .priority(mealRecipe.getPriority())
                .build();
    }
}
