package ru.sushi.delivery.kds.domain;

import lombok.Data;

@Data
public class Ingredient {
    private final long id;
    private final String name;
    private final Measurement measurementUnit;
    private final int amount;

    @Override
    public String toString() {
        return String.format("%s %s%s", name, amount, measurementUnit.getName());
    }
}
