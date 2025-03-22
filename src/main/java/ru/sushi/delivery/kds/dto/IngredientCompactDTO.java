package ru.sushi.delivery.kds.dto;

import lombok.Builder;
import lombok.Data;
import ru.sushi.delivery.kds.domain.controller.dto.MealRecipeDto;

@Data
@Builder
public class IngredientCompactDTO {
    private final String name;
    private final Long stationId;

    public static IngredientCompactDTO of(MealRecipeDto ingredientData) {
        String name = String.format("%s - %.1f%s",
                ingredientData.getSourceName(),
                ingredientData.getInitAmount(),
                ingredientData.getMeasurementUnit().getName()
        );
        return IngredientCompactDTO.builder()
                .name(name)
                .stationId(ingredientData.getStationId())
                .build();
    }
}
