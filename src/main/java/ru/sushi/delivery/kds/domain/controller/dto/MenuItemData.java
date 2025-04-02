package ru.sushi.delivery.kds.domain.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sushi.delivery.kds.domain.persist.entity.product.MenuItem;

import java.time.Duration;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemData {

    private String flow;

    private Double price;

    private Double fcPrice;

    private Long id;

    private String name;

    private Duration timeToCook;

    public static MenuItemData of(MenuItem menuItem) {
        return MenuItemData.builder()
                .flow(menuItem.getFlow().getName())
                .id(menuItem.getId())
                .name(menuItem.getName())
                .price(menuItem.getPrice())
                .fcPrice(menuItem.getFcPrice())
                .timeToCook(menuItem.getTimeToCook())
                .build();
    }
}
