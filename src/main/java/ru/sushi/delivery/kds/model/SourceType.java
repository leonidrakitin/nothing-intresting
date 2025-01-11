package ru.sushi.delivery.kds.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SourceType {
    INGREDIENT("Ингридиент"),
    PREPACK("Полуфабрикат");

    private final String value;
}
