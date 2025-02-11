package ru.sushi.delivery.kds.domain.persist.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sushi.delivery.kds.domain.persist.entity.product.WriteOffItem;
import ru.sushi.delivery.kds.wrappers.WriteOffItemWrapper;

@Repository
public interface WriteOffItemRepository extends JpaRepository<WriteOffItem, Long> {

    @Query("""
                        select new ru.sushi.delivery.kds.wrappers.WriteOffItemWrapper(
                            wi.id,
                            wi.sourceType,
                            case 
                                when wi.sourceType = ru.sushi.delivery.kds.model.SourceType.INGREDIENT then i.name
                                when wi.sourceType = ru.sushi.delivery.kds.model.SourceType.PREPACK then p.name
                                else ''
                            end ,
                            wi.sourceId,
                            wi.amount,
                            wi.discontinuedComment,
                            wi.discontinuedReason,
                            wi.isCompleted,
                            wi.createdBy,
                            wi.createdAt
                        )
                        from WriteOffItem wi
                        left join IngredientItem ii 
                            on ii.id = wi.sourceId and wi.sourceType = ru.sushi.delivery.kds.model.SourceType.INGREDIENT
                        left join Ingredient i on i.id = ii.ingredient.id
                        left join PrepackItem pi 
                            on pi.id = wi.sourceId and wi.sourceType = ru.sushi.delivery.kds.model.SourceType.PREPACK
                        left join Prepack p on p.id = pi.prepack.id
            """)
    Page<WriteOffItemWrapper> findAllWithName(Pageable pageable);
}