package ru.sushi.delivery.kds.domain.persist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sushi.delivery.kds.domain.persist.entity.Order;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("""
        select o from Order o
        left join fetch o.orderItems oi
        where o.status != 'READY' and o.status != 'CANCELED'
        group by o
        order by max(oi.stationChangedAt) desc
    """)
    List<Order> findAllActive();
}