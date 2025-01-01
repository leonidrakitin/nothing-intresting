package ru.sushi.delivery.kds.domain.persist.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class Measurement implements Identifiable<Long> {
    private final Long id;
    private final String name;

    public Measurement(String name) {
        this.id = (long) (Math.random()*100000);
        this.name = name;
    }
}
