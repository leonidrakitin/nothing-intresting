package ru.sushi.delivery.kds.domain.persist.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.sushi.delivery.kds.domain.model.OrderStatus;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class Station implements Identifiable<Long> {
    private final Long id;
    private final String name;
    private final OrderStatus orderStatusAtStation;
//    private final List<Screen> displays = new ArrayList<>();
}
