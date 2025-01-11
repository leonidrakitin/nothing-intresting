package ru.sushi.delivery.kds.domain.service;

import com.vaadin.flow.router.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.persist.entity.product.Ingredient;
import ru.sushi.delivery.kds.domain.persist.repository.product.IngredientRepository;
import ru.sushi.delivery.kds.dto.IngredientDTO;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final Map<Long, List<Ingredient>> ingredientCache = new ConcurrentHashMap<>();

//    @PostConstruct
//    public void initializeCache() {
//        List<Ingredient> ingredients = ingredientRepository.findAll();
//        ingredientCache.putAll(
//                ingredients.stream().collect(
//                        Collectors.groupingBy(
//                                ingredient -> ingredient.getPosition().getId(),
//                                Collectors.toList()
//                        )
//                )
//        );
//    }

    public List<IngredientDTO> getPositionIngredients(Long positionId) {
        return this.ingredientCache.getOrDefault(positionId, List.of()).stream()
                .map(ingredient -> IngredientDTO.builder()
                                .name(ingredient.getName())
//                            .stationId(ingredient.getStationId()) //TODO from recipe
                                .build()
                )
                .toList();
    }

    public Ingredient get(Long id) {
        return this.ingredientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ingredient not found id " + id));
    }
}

