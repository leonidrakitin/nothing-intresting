package ru.sushi.delivery.kds.domain;

import lombok.Data;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Data
public class Order {
    private final long id;
    private final String name;
    private final List<Item> items;
    private final Instant createdAt;
    private final Instant finishedAt;
    private final LinkedList<Long> screensFlow;
}
