package ru.sushi.delivery.kds.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sushi.delivery.kds.model.OrderStatus;

import java.util.List;

// Пример DTO для отображения всего заказа и позиций
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderFullDto {
    private Long id;
    private String name;
    private OrderStatus status;
    private List<OrderItemDto> items;
}

