package ru.sushi.delivery.kds.domain.persist.repository.recipe;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sushi.delivery.kds.domain.persist.entity.recipe.MealRecipe;
import ru.sushi.delivery.kds.domain.persist.entity.recipe.Recipe;
import ru.sushi.delivery.kds.model.SourceType;

import java.util.List;

@Repository
public interface MealRecipeRepository extends JpaRepository<MealRecipe, Long> {

    @Query("""
        select recipe from MealRecipe recipe
        where recipe.meal.id in :mealIds
    """)
    List<Recipe> findByMealIds(List<Long> mealId);

    List<MealRecipe> findByMealId(Long id);

    List<MealRecipe> findAllBySourceIdAndSourceType(Long sourceId, SourceType sourceType);
}
