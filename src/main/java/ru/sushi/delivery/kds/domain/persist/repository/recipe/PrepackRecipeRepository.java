package ru.sushi.delivery.kds.domain.persist.repository.recipe;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sushi.delivery.kds.domain.persist.entity.recipe.PrepackRecipe;
import ru.sushi.delivery.kds.model.SourceType;

import java.util.List;

@Repository
public interface PrepackRecipeRepository extends JpaRepository<PrepackRecipe, Long> {
    List<PrepackRecipe> findByPrepackId(Long prepackId);

    List<PrepackRecipe> findAllBySourceIdAndSourceType(Long sourceId, SourceType sourceType);
}
