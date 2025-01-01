package ru.sushi.delivery.kds.domain.persist.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class Screen implements Identifiable<String> {
    private final String id;
    private final Station station;

    public Screen(Station station) {
        this.id = new Double(Math.random()*1000000).toString();
        this.station = station;
    }
}
