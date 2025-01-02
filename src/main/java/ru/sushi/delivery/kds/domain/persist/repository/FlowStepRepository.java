package ru.sushi.delivery.kds.domain.persist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sushi.delivery.kds.domain.persist.entity.FlowStep;

@Repository
public interface FlowStepRepository extends JpaRepository<FlowStep, Long> {
}
