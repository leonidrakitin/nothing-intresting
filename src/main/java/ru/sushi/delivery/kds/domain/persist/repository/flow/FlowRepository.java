package ru.sushi.delivery.kds.domain.persist.repository.flow;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sushi.delivery.kds.domain.persist.entity.flow.Flow;

import java.util.Optional;

@Repository
public interface FlowRepository extends JpaRepository<Flow, Long> {

    Optional<Flow> getFlowByName(String name);
}
