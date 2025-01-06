package ru.sushi.delivery.kds.domain.persist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sushi.delivery.kds.domain.persist.entity.ItemRecipe;

@Repository
public interface ItemRecipeRepository extends JpaRepository<ItemRecipe, Long> {
}
