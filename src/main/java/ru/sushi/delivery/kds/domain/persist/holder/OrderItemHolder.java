package ru.sushi.delivery.kds.domain.persist.holder;

import org.springframework.stereotype.Component;
import ru.sushi.delivery.kds.domain.persist.entity.Order;
import ru.sushi.delivery.kds.domain.persist.entity.OrderItem;

import java.util.List;

@Component
public class OrderItemHolder extends AbstractInMemoryHolder<OrderItem, Long> {
}