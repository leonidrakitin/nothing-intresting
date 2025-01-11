package ru.sushi.delivery.kds.domain.persist.repository.flow;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sushi.delivery.kds.domain.persist.entity.flow.Station;
import ru.sushi.delivery.kds.model.OrderStatus;

@Repository
public interface StationRepository extends JpaRepository<Station, Long> {

    Station findByOrderStatusAtStation(OrderStatus orderStatusAtStation);
}