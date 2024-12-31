package ru.sushi.delivery.kds.persist.model;

import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Data
public class OrderItem {

    private static final AtomicLong COUNTER = new AtomicLong();

    private final long id;
    private final String name;
    private final List<Ingredient> details;
    private final Instant createdAt;
    private final Long nextScreen;

    public OrderItem(String name, List<Ingredient> details, Long nextScreen) {
        this.id = COUNTER.incrementAndGet();
        this.name = name;
        this.details = details;
        this.createdAt = Instant.now();
        this.nextScreen = nextScreen;
    }

    public OrderItem(String name, List<Ingredient> details) {
        this.id = COUNTER.incrementAndGet();
        this.name = name;
        this.details = details;
        this.createdAt = Instant.now();
        this.nextScreen = null;
    }
}
