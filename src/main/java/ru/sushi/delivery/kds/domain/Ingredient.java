package ru.sushi.delivery.kds.domain;

import lombok.Data;

import java.text.DecimalFormat;

@Data
public class Ingredient {
    private final long id;
    private final String name;
    private final Measurement measurementUnit;
    private final double amount;

    @Override
    public String toString() {
        return String.format("%s %s%s", name, new DecimalFormat("#.##########").format(amount), measurementUnit.getName());
    }
}
