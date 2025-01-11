package ru.sushi.delivery.kds.domain.persist.repository.recipe;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sushi.delivery.kds.domain.persist.entity.recipe.PositionRecipe;
import ru.sushi.delivery.kds.domain.persist.entity.recipe.Recipe;

import java.util.List;

@Repository
public interface PositionRecipeRepository extends JpaRepository<PositionRecipe, Long> {

    @Query("""
        select recipe from PositionRecipe recipe
        where recipe.position.id in :positionIds
    """)
    List<Recipe> findByPositionIds(List<Long> positionId);
}
