package ru.sushi.delivery.kds.domain.persist.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor //todo remove
@Builder(toBuilder = true)
public class ItemSet implements Identifiable<Long> {
    private final Long id;
    private final String name;
    private final List<Item> items;
}
