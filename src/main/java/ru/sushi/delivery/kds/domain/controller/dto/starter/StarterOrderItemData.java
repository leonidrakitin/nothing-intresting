package ru.sushi.delivery.kds.domain.controller.dto.starter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class StarterOrderItemData {

    private final int itemId;
    private final String name;
    private final int quantity;
    private final int price;
}
