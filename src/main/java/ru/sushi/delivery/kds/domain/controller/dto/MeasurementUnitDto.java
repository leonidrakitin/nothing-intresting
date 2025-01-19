package ru.sushi.delivery.kds.domain.controller.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MeasurementUnitDto {
    private final Long id;
    private final String name;
}
