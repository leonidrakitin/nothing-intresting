package ru.sushi.delivery.kds.dto;

import lombok.Builder;
import lombok.Data;
import ru.sushi.delivery.kds.domain.persist.entity.Ingredient;
import ru.sushi.delivery.kds.domain.persist.entity.Station;
import ru.sushi.delivery.kds.model.OrderItemStationStatus;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class OrderItemDto {

    private final Long id;
    private final Long orderId;
    private final String name;
    private final List<IngredientDTO> ingredients;
    private final Instant createdAt;
    //TODO Так делать плохо
    private OrderItemStationStatus status;
    private Station currentStation;
}
