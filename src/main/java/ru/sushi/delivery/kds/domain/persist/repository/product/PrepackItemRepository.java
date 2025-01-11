package ru.sushi.delivery.kds.domain.persist.repository.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sushi.delivery.kds.domain.persist.entity.product.PrepackItem;
import ru.sushi.delivery.kds.domain.persist.entity.product.SourceItem;

import java.util.List;

@Repository
public interface PrepackItemRepository extends JpaRepository<PrepackItem, Long> {

    @Query("""
        select i from PrepackItem i
        where i.prepack.id = :itemId and i.discontinuedAt is null
        order by i.createdAt desc
    """)
    List<SourceItem> findActiveByPrepackId(long itemId);
}

