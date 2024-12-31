package ru.sushi.delivery.kds.domain;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Data
public class Item {
    private final long id;
    private final String name;
    private final LinkedList<PlaceType> placesFlow;
    private final List<Ingredient> ingredients;
    private final PlaceType currentStep = BusinessLogic.PLACE_TYPE_DEFAULT;
}
