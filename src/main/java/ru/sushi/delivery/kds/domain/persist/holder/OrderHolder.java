package ru.sushi.delivery.kds.domain.persist.holder;

import org.springframework.stereotype.Component;
import ru.sushi.delivery.kds.domain.persist.entity.Measurement;
import ru.sushi.delivery.kds.domain.persist.entity.Order;

import java.util.List;

@Component
public class OrderHolder extends AbstractInMemoryHolder<Order, Long> {
}