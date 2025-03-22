package ru.sushi.delivery.kds.domain.persist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sushi.delivery.kds.domain.persist.entity.ItemCombo;

import java.util.List;

@Repository
public interface ItemComboRepository extends JpaRepository<ItemCombo, Long> {

    @Query("select ic from ItemCombo ic left join fetch ic.meals")
    List<ItemCombo> findAll();
}
