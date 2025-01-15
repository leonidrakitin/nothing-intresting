package ru.sushi.delivery.kds.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IngredientCompactDTO {
    private final String name;
    private final Long stationId;
}
