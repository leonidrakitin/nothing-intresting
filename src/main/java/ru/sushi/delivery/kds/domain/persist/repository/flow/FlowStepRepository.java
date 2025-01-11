package ru.sushi.delivery.kds.domain.persist.repository.flow;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sushi.delivery.kds.domain.persist.entity.flow.FlowStep;

@Repository
public interface FlowStepRepository extends JpaRepository<FlowStep, Long> {

    FlowStep getFlowStepByStation_IdAndFlow_Id(Long stationId,Long flowId);
}
