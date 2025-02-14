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
        select oi from OrderItem oi
        left join MenuItem mi on mi.id = oi.menuItem.id
        left join FlowStep step on step.flow.id = mi.flow.id and step.stepOrder = oi.currentFlowStep
        where step.station.id = :stationId
        order by
            case when oi.status = 'STARTED' then 0 else 1 end,
            case when oi.status = 'STARTED' then oi.statusUpdatedAt end,
            oi.order.createdAt,
            oi.menuItem.productType.id
    """)
    List<OrderItem> findAllItemsByStationId(Long stationId);

    void deleteById(Long id);
}