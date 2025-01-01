package ru.sushi.delivery.kds.domain.persist.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.text.DecimalFormat;

@Data
@AllArgsConstructor //todo remove
@Builder(toBuilder = true)
public class Ingredient implements Identifiable<Long> {

    private final Long id;
    private final String name;
    private final Measurement measurementUnit;
    private final double amount;

    @Override
    public String toString() {
        return String.format("%s %s%s", name, new DecimalFormat("#.##########").format(amount), measurementUnit.getName());
    }
}
