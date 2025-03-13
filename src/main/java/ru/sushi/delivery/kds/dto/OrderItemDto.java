package ru.sushi.delivery.kds.dto;

import lombok.Builder;
import lombok.Data;
import ru.sushi.delivery.kds.domain.persist.entity.flow.Station;
import ru.sushi.delivery.kds.model.FlowStepType;
import ru.sushi.delivery.kds.model.OrderItemStationStatus;

import java.time.Instant;
import java.util.List;

@Data
@Builder(toBuilder = true)
public class OrderItemDto {

    private final Long id;
    private final Long orderId;
    private final String orderName;
    private final String name;
    private final List<IngredientCompactDTO> ingredients;
    private final Instant statusUpdatedAt;
    private final Instant createdAt = Instant.now(); //обратная совместимость для старых версий
    private final int timeToCook;
    //TODO Так делать плохо
    private OrderItemStationStatus status;
    private Station currentStation;
    private FlowStepType flowStepType;
    private boolean extra;
}
