package ru.sushi.delivery.kds.domain.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sushi.delivery.kds.domain.persist.entity.recipe.MenuItemRecipe;

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
    private Long priority;

    public static MenuItemRecipeDto of(MenuItemRecipe menuItemRecipe, String sourceName) {
        return MenuItemRecipeDto.builder()
                .id(menuItemRecipe.getId())
                .sourceName(String.format("%s [%s]", sourceName, menuItemRecipe.getSourceType().name()))
                .measurementUnit(new MeasurementUnitDto(
                        menuItemRecipe.getMeasurement().getId(),
                        menuItemRecipe.getMeasurement().getName()
                ))
                .stationId(menuItemRecipe.getStationId())
                .initAmount(menuItemRecipe.getInitAmount())
                .finalAmount(menuItemRecipe.getFinalAmount())
                .lossesAmount(menuItemRecipe.getLossesAmount())
                .lossesPercentage(menuItemRecipe.getLossesPercentage())
                .priority(menuItemRecipe.getPriority())
                .build();
    }
}
