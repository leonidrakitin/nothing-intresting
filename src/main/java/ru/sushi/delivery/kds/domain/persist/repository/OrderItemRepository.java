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
        left join FlowStep step on step.id = o.currentFlowStepId and step.flow.id = o.flowId
        where step.station.id = :stationId
    """)
    List<OrderItem> findAllItemsByStationId(Long stationId);
}