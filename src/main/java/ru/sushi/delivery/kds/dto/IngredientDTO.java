package ru.sushi.delivery.kds.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IngredientDTO {

    private final String name;
    private final Long stationId;

}
