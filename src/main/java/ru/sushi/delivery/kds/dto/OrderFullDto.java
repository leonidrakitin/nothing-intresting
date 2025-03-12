package ru.sushi.delivery.kds.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sushi.delivery.kds.domain.persist.entity.Order;
import ru.sushi.delivery.kds.model.OrderStatus;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderFullDto {
    private Long id;
    private String name;
    private OrderStatus status;
    private List<OrderItemDto> items;
    private Instant shouldBeFinishedAt;
    private Instant kitchenShouldGetOrderAt;
    private Instant kitchenGotOrderAt;

    public static OrderFullDto of(Order order, List<OrderItemDto> orderItems) {
        return OrderFullDto.builder()
                .id(order.getId())
                .name(order.getName())
                .status(order.getStatus())
                .items(orderItems)
                .shouldBeFinishedAt(order.getShouldBeFinishedAt())
                .kitchenShouldGetOrderAt(order.getKitchenShouldGetOrderAt())
                .kitchenGotOrderAt(order.getKitchenGotOrderAt())
                .build();
    }
}

