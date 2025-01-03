package ru.sushi.delivery.kds.domain.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.persist.entity.Ingredient;
import ru.sushi.delivery.kds.domain.persist.repository.IngredientRepository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IngredientCacheService {

    private final IngredientRepository ingredientRepository;
    private final Map<Long, List<Ingredient>> ingredientCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void initializeCache() {
        List<Ingredient> ingredients = ingredientRepository.findAll();
        ingredientCache.putAll(
                ingredients.stream().collect(
                        Collectors.groupingBy(
                                ingredient -> ingredient.getItem().getId(),
                                Collectors.toList()
                        )
                )
        );
    }

    public List<Ingredient> getItemIngredients(Long itemId) {
        return ingredientCache.getOrDefault(itemId, List.of());
    }
}

