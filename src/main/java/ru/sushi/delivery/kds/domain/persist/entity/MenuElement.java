package ru.sushi.delivery.kds.domain.persist.entity;

import java.util.List;

public interface MenuElement extends Identifiable<Long> {

        String getName();

        List<Item> getItems();
    }
