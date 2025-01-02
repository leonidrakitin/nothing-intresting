package ru.sushi.delivery.kds.domain.persist.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.sushi.delivery.kds.domain.model.OrderItemStationStatus;

import java.time.Instant;
import java.util.Iterator;
import java.util.ListIterator;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class OrderItem implements Identifiable<Long> {
    private final Long id;
    private final Long orderId;
    private final Item item;
    private final ListIterator<Station> stationsIterator;
    private final OrderItemStationStatus status;
    private final Instant statusUpdatedAt;

    public OrderItem(Long orderId, Item item) {
        this.id = (long) (Math.random() * 1000000);
        this.orderId = orderId;
        this.item = item;
        this.stationsIterator = item.getStationsIterator().listIterator();
        this.status = OrderItemStationStatus.ADDED;
        this.statusUpdatedAt = Instant.now();
        stationsIterator.next();
    }

    //todo workflow!!
    public Station getCurrentStation() {
            if (stationsIterator.hasPrevious()) {
            Station station = stationsIterator.previous();
            stationsIterator.next();
            return station;
        } else {
            return item.getStationsIterator().iterator().next();
        }
    }
}
