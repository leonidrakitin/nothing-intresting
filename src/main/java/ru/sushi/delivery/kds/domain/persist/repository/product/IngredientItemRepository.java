package ru.sushi.delivery.kds.domain.persist.repository.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sushi.delivery.kds.domain.persist.entity.product.IngredientItem;
import ru.sushi.delivery.kds.domain.persist.entity.product.SourceItem;

import java.util.List;

@Repository
public interface IngredientItemRepository extends JpaRepository<IngredientItem, Long> {

    @Query("""
        select i from IngredientItem i
        where i.ingredient.id = :itemId and i.discontinuedAt is null
        order by i.createdAt desc
    """)
    List<SourceItem> findActiveByIngredientId(long itemId);
}

