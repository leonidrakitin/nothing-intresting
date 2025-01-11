package ru.sushi.delivery.kds.domain.persist.repository.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sushi.delivery.kds.domain.persist.entity.product.Position;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {
}