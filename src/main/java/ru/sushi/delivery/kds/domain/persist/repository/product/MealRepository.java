package ru.sushi.delivery.kds.domain.persist.repository.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sushi.delivery.kds.domain.persist.entity.product.Meal;

import java.util.List;

@Repository
public interface MealRepository extends JpaRepository<Meal, Long> {

    @Query("""
            select i from Meal i
            left join fetch i.productType oi
            where oi.extra = true
            """)
    List<Meal> findAllExtras();
}