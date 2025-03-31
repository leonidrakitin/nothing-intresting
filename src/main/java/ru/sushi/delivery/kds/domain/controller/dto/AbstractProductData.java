package ru.sushi.delivery.kds.domain.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Duration;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class AbstractProductData {

    private Long id;
    private String name;
    private String measurementUnitName;
    private Duration expirationDuration;
    private Double notifyAfterAmount;
    private Double fcPrice;
}
