package ru.sushi.delivery.kds.domain.persist.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.sushi.delivery.kds.domain.model.OrderStatus;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class Order implements Identifiable<Long> {
    private final Long id;
    private final String name;
    private final List<OrderItem> items; //todo remove it
    private final OrderStatus status;
    private final Instant statusUpdateAt;

    public Order(String name, List<OrderItem> items) {
        this.id = new Double(Math.random()*1000000).longValue();
        this.name = name;
        this.items = items;
        this.status = OrderStatus.CREATED;
        this.statusUpdateAt = Instant.now();
    }
}
