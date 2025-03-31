package ru.sushi.delivery.kds.domain.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sushi.delivery.kds.domain.persist.entity.product.Product;
import ru.sushi.delivery.kds.domain.persist.entity.recipe.PrepackRecipe;

import java.util.Optional;

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
    private Double fcCost;

    public static PrepackRecipeData of(PrepackRecipe prepackRecipe, String sourceName, Product product) {
        double fcPrice = Optional.ofNullable(product.getFcPrice()).orElse(0.0);
        if (product.getMeasurementUnit().getId() == 1) {
            fcPrice += fcPrice == 0
                    ? 0.0
                    : fcPrice / prepackRecipe.getInitAmount();
        } else {
            fcPrice += fcPrice / 100 * prepackRecipe.getInitAmount();
        }
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
                .fcCost(fcPrice)
                .build();
    }
}
