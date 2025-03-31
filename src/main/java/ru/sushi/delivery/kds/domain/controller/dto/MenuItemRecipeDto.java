package ru.sushi.delivery.kds.domain.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sushi.delivery.kds.domain.persist.entity.product.Product;
import ru.sushi.delivery.kds.domain.persist.entity.recipe.MenuItemRecipe;

import java.util.Optional;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemRecipeDto {

    private Long id;
    private String sourceName;
    private MeasurementUnitDto measurementUnit;
    private Long stationId;
    private Double initAmount;
    private Double finalAmount;
    private Double lossesAmount;
    private Double lossesPercentage;
    private Double fcCost;
    private Long priority;

    public static MenuItemRecipeDto of(MenuItemRecipe menuItemRecipe, String sourceName, Product product) {
        double fcPrice = Optional.ofNullable(product.getFcPrice()).orElse(0.0);
        if (product.getMeasurementUnit().getId() == 1) {
            fcPrice = fcPrice == 0
                    ? 0.0
                    : fcPrice / menuItemRecipe.getInitAmount();
        } else {
            fcPrice = fcPrice / 100 * menuItemRecipe.getInitAmount();
        }
        return MenuItemRecipeDto.builder()
                .id(menuItemRecipe.getId())
                .sourceName(sourceName) //todo everywhere except kitchen String.format("%s [%s]", sourceName, menuItemRecipe.getSourceType().name())
                .measurementUnit(new MeasurementUnitDto(
                        menuItemRecipe.getMeasurement().getId(),
                        menuItemRecipe.getMeasurement().getName()
                ))
                .stationId(menuItemRecipe.getStationId())
                .initAmount(menuItemRecipe.getInitAmount())
                .finalAmount(menuItemRecipe.getFinalAmount())
                .lossesAmount(menuItemRecipe.getLossesAmount())
                .lossesPercentage(menuItemRecipe.getLossesPercentage())
                .fcCost(fcPrice)
                .priority(menuItemRecipe.getPriority())
                .build();
    }
}
