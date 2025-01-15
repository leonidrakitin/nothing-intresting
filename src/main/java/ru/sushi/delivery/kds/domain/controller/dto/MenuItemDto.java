package ru.sushi.delivery.kds.domain.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sushi.delivery.kds.domain.persist.entity.product.MenuItem;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemDto {

    private String flow;

    private Long id;

    private String name;

    public static MenuItemDto of(MenuItem menuItem) {
        return MenuItemDto.builder()
                .flow(menuItem.getFlow().getName())
                .id(menuItem.getId())
                .name(menuItem.getName())
                .build();
    }
}
