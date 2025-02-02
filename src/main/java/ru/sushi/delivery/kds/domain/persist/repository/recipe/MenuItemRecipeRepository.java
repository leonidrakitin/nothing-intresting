package ru.sushi.delivery.kds.domain.persist.repository.recipe;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sushi.delivery.kds.domain.persist.entity.recipe.MenuItemRecipe;
import ru.sushi.delivery.kds.domain.persist.entity.recipe.Recipe;

import java.util.List;

@Repository
public interface MenuItemRecipeRepository extends JpaRepository<MenuItemRecipe, Long> {

    @Query("""
        select recipe from MenuItemRecipe recipe
        where recipe.menuItem.id in :menuItemIds
    """)
    List<Recipe> findByMenuItemIds(List<Long> menuItemId);

    List<MenuItemRecipe> findByMenuItemId(Long id);

    List<MenuItemRecipe> findAllBySourceId(Long sourceId);
}
