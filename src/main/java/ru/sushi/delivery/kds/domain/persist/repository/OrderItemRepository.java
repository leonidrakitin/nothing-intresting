package ru.sushi.delivery.kds.domain.persist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sushi.delivery.kds.domain.persist.entity.OrderItem;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    @Query("""
            select o from OrderItem o
            left join Item i on i.id = o.item.id
            left join FlowStep step on step.flow.id = i.flow.id and step.stepOrder = o.currentFlowStep
            where step.station.id = :stationId
            order by o.stationChangedAt
    """)
    List<OrderItem> findAllItemsByStationId(Long stationId);

    void deleteById(Long id);
}