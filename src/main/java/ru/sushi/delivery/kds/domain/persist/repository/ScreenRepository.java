package ru.sushi.delivery.kds.domain.persist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sushi.delivery.kds.domain.persist.entity.Screen;

@Repository
public interface ScreenRepository extends JpaRepository<Screen, Long> {

    Screen findByStationId(Long stationId);
}