package ru.sushi.delivery.kds.domain.persist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.sushi.delivery.kds.domain.persist.entity.OrderItem;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    @Query("""
        select oi from OrderItem oi
        where oi.order.id in :orderIds
    """)
    List<OrderItem> findByOrderIdIn(@Param("orderIds") List<Long> orderIds);

    void deleteById(Long id);
}