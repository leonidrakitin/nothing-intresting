package ru.sushi.delivery.kds.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderItemStationStatus {
    ADDED("Ожидает"),
    STARTED("Взят в работу"),
    COMPLETED("Закончен"),
    CANCELED("Отменен");

    private final String name;
}
