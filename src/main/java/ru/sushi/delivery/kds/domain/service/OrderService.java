package ru.sushi.delivery.kds.domain.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.model.OrderItemStationStatus;
import ru.sushi.delivery.kds.domain.model.OrderStatus;
import ru.sushi.delivery.kds.domain.persist.entity.Item;
import ru.sushi.delivery.kds.domain.persist.entity.Order;
import ru.sushi.delivery.kds.domain.persist.entity.OrderItem;
import ru.sushi.delivery.kds.domain.persist.entity.Station;
import ru.sushi.delivery.kds.domain.persist.holder.OrderHolder;
import ru.sushi.delivery.kds.domain.persist.holder.OrderItemHolder;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderHolder orderHolder;
    private final OrderItemHolder orderItemHolder;

    public void createOrder(List<Item> items) {
        Order order = orderHolder.save(new Order("23123-31313", List.of()));
        List<OrderItem> orderItems = items.stream()
                .map(item -> new OrderItem(order.getId(), item))
                .toList();
        orderItems.forEach(orderItemHolder::save);
        orderHolder.save(order.toBuilder().items(orderItems).build());
    }

    public Collection<OrderItem> getOrderItems() {
        return this.orderItemHolder.findAll();
    }

    public void updateOrderItem(Long orderItemId) {
        OrderItem orderItem = this.orderItemHolder.getOrThrow(orderItemId);

        orderItem = switch (orderItem.getStatus()) {
            case ADDED -> orderItem.toBuilder()
                    .status(OrderItemStationStatus.STARTED)
                    .statusUpdatedAt(Instant.now())
                    .build();
            case STARTED -> orderItem.toBuilder()
                    .status(OrderItemStationStatus.COMPLETED)
                    .statusUpdatedAt(Instant.now())
                    .build();
            case COMPLETED -> orderItem;
        };
        if (orderItem.getStatus() == OrderItemStationStatus.COMPLETED) {
            if (orderItem.getStationsIterator().hasNext()) {
                orderItem.toBuilder().status(OrderItemStationStatus.ADDED).build();
                Station station = orderItem.getStationsIterator().next();
                //add notification to all station displays
            }
        }
        this.orderItemHolder.save(orderItem);

        Order order = orderHolder.getOrThrow(orderItem.getOrderId());
        this.updateOrderStatus(order);
    }

    public Order updateOrderStatus(Order order) {

        if (OrderStatus.READY == order.getStatus()) {
            return order;
        }

        int minimumStatus = 1;

        for (OrderItem orderItem : order.getItems()) {
            Station currentStation = orderItem.getCurrentStation();
            int currentPriorityStatus = this.definePriorityByOrderStatus(currentStation.getOrderStatusAtStation());
            if (currentPriorityStatus > 0 && minimumStatus == 0) {
                minimumStatus = 1;
            } else {
                minimumStatus = Math.min(minimumStatus, currentPriorityStatus);
            }
        }

        OrderStatus newOrderStatus = this.defineOrderStatusByPriority(minimumStatus);

        if (newOrderStatus != order.getStatus()) {
            return orderHolder.save(order.toBuilder()
                    .status(newOrderStatus)
                    .build()
            );
        }

        return order;
    }

    private int definePriorityByOrderStatus(OrderStatus orderStatus) {
        return switch (orderStatus) {
            case CREATED -> 0;
            case COOKING -> 1;
            case COLLECTING -> 2;
            case READY -> 3;
        };
    }

    private OrderStatus defineOrderStatusByPriority(int priority) {
        return switch (priority) {
            case 0 -> OrderStatus.CREATED;
            case 1 -> OrderStatus.COOKING;
            case 2 -> OrderStatus.COLLECTING;
            case 3 -> OrderStatus.READY;
            default -> throw new IllegalStateException("Unexpected value: " + priority);
        };
    }
}
