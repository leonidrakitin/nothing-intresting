package ru.sushi.delivery.kds.persist.model;

import lombok.Data;

@Data
public class Ingredient {
    final String name;
    final String measurementUnit;
    final int amount;

    @Override
    public String toString() {
        return String.format("%s %s%s", name, amount, measurementUnit);
    }
}
