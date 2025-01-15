package ru.sushi.delivery.kds.domain.service;

import com.vaadin.flow.router.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.controller.dto.IngredientDto;
import ru.sushi.delivery.kds.domain.persist.entity.Measurement;
import ru.sushi.delivery.kds.domain.persist.entity.product.Ingredient;
import ru.sushi.delivery.kds.domain.persist.repository.MeasurementRepository;
import ru.sushi.delivery.kds.domain.persist.repository.product.IngredientRepository;
import ru.sushi.delivery.kds.dto.IngredientCompactDTO;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final MeasurementRepository measurementRepository;
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

    public List<IngredientCompactDTO> getMenuItemIngredients(Long menuItemId) {
        return this.ingredientCache.getOrDefault(menuItemId, List.of()).stream()
                .map(ingredient -> IngredientCompactDTO.builder()
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

    public List<IngredientDto> getAllIngredients() {
        return this.ingredientRepository.findAll().stream().map(IngredientDto::of).toList();
    }

    public void saveIngredient(IngredientDto ingredientDTO) {
        Measurement measurement = measurementRepository.findByName(ingredientDTO.getMeasurementUnitName())
                .orElseThrow(() -> new IllegalArgumentException("Invalid measurement unit"));

        Ingredient ingredient = new Ingredient();
        ingredient.setName(ingredientDTO.getName());
        ingredient.setPieceInGrams(ingredientDTO.getPieceInGrams());
        ingredient.setExpirationDuration(ingredientDTO.getExpirationDuration());
        ingredient.setNotifyAfterAmount(ingredientDTO.getNotifyAfterAmount());
        ingredient.setMeasurementUnit(measurement);

        ingredientRepository.save(ingredient);
    }

}

