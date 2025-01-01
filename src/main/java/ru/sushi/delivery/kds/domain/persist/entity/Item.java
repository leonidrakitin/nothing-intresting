package ru.sushi.delivery.kds.domain.persist.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Iterator;
import java.util.List;

@Data
@AllArgsConstructor //todo remove
@Builder(toBuilder = true)
public class Item implements Identifiable<Long> {
    private final Long id;
    private final String name;
    private final List<Station> stationsIterator;
    private final List<Ingredient> ingredients;
}
