package ru.sushi.delivery.kds.domain.persist.repository.flow;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sushi.delivery.kds.domain.persist.entity.flow.Screen;

@Repository
public interface ScreenRepository extends JpaRepository<Screen, String> {
}