package ru.sushi.delivery.kds.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PlaceType {
    private final long id;
    private final String name;
    private final List<Long> displays = new ArrayList<>();
}
