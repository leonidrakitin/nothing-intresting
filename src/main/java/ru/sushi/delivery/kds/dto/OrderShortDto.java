package ru.sushi.delivery.kds.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sushi.delivery.kds.domain.persist.entity.Order;
import ru.sushi.delivery.kds.model.OrderStatus;
import ru.sushi.delivery.kds.model.OrderType;
import ru.sushi.delivery.kds.model.PaymentType;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderShortDto {
    private Long id;
    private String name;
    private OrderStatus status;
    private List<OrderItemDto> items;
    private Instant shouldBeFinishedAt;
    private Instant kitchenShouldGetOrderAt;
    private Instant kitchenGotOrderAt;
    private OrderType orderType;
    private OrderAddressDto address;
    private String customerPhone;
    private PaymentType paymentType;

    public static OrderShortDto of(Order order, List<OrderItemDto> orderItems) {
        return OrderShortDto.builder()
                .id(order.getId())
                .name(order.getName() + (order.getPreorder() ? "[предзаказ]" : ""))
                .status(order.getStatus())
                .items(orderItems)
                .shouldBeFinishedAt(order.getShouldBeFinishedAt())
                .kitchenShouldGetOrderAt(order.getKitchenShouldGetOrderAt())
                .kitchenGotOrderAt(order.getKitchenGotOrderAt())
                .orderType(order.getOrderType())
                .address(OrderAddressDto.of(order.getAddress()))
                .customerPhone(order.getCustomerPhone())
                .paymentType(order.getPaymentType())
                .build();
    }
}

