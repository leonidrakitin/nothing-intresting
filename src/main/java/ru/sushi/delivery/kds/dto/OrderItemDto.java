package ru.sushi.delivery.kds.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class OrderItemDto {

    private final Long id;
    private final Long orderId;
    private final String name;
    private final List<String> ingredients;
    private final Instant createdAt;
}
