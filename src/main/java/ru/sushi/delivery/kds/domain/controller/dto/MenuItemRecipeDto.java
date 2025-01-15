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
    private Long stationId;
    private Double initAmount;
    private Double finalAmount;
    private Double lossesAmount;
    private Double lossesPercentage;

    public static MenuItemRecipeDto of(MenuItemRecipe menuItemRecipe, String sourceName) {
        return MenuItemRecipeDto.builder()
                .id(menuItemRecipe.getId())
                .sourceName(sourceName)
                .stationId(menuItemRecipe.getStationId())
                .initAmount(menuItemRecipe.getInitAmount())
                .finalAmount(menuItemRecipe.getFinalAmount())
                .lossesAmount(menuItemRecipe.getLossesAmount())
                .lossesPercentage(menuItemRecipe.getLossesPercentage())
                .build();
    }
}
