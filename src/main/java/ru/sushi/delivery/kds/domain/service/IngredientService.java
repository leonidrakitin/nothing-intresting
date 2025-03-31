package ru.sushi.delivery.kds.domain.service;

import com.vaadin.flow.router.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.controller.dto.IngredientDto;
import ru.sushi.delivery.kds.domain.controller.dto.MenuItemRecipeDto;
import ru.sushi.delivery.kds.domain.persist.entity.Measurement;
import ru.sushi.delivery.kds.domain.persist.entity.product.Ingredient;
import ru.sushi.delivery.kds.domain.persist.repository.MeasurementRepository;
import ru.sushi.delivery.kds.domain.persist.repository.product.IngredientRepository;
import ru.sushi.delivery.kds.dto.IngredientCompactDTO;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final MeasurementRepository measurementRepository;
    private final Map<Long, List<Ingredient>> ingredientCache = new ConcurrentHashMap<>();
    private final RecipeService recipeService;

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
        return this.recipeService.getMenuRecipeByMenuId(menuItemId).stream()
                .sorted(Comparator.comparingLong(MenuItemRecipeDto::getPriority))
                .map(IngredientCompactDTO::of)
                .toList();
    }

    public Ingredient get(Long id) {
        return this.ingredientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ingredient not found id " + id));
    }

    public List<IngredientDto> getAllIngredients() {
        return this.ingredientRepository.findAll().stream().map(IngredientDto::of).toList();
    }

    public void delete(IngredientDto ingredientDto) {
        this.ingredientRepository.deleteById(ingredientDto.getId());
    }

    public void save(IngredientDto ingredientData) {
        Measurement measurement = measurementRepository.findByName(ingredientData.getMeasurementUnitName())
                .orElseThrow(() -> new IllegalArgumentException("Invalid measurement unit"));

        Ingredient ingredient = Optional.ofNullable(ingredientData.getId())
                .map(this.ingredientRepository::findById)
                .flatMap(Function.identity())
                .map(i -> this.setNewIngredientData(i, ingredientData, measurement))
                .orElseGet(() -> Ingredient.of(ingredientData, measurement));

        this.ingredientRepository.save(ingredient);
    }

    public Ingredient setNewIngredientData(
            Ingredient ingredient,
            IngredientDto ingredientData,
            Measurement measurement
    ) {
        return ingredient.toBuilder()
                .id(ingredientData.getId())
                .name(ingredientData.getName())
                .fcPrice(ingredientData.getFcPrice())
                .pieceInGrams(ingredientData.getPieceInGrams())
                .expirationDuration(ingredientData.getExpirationDuration())
                .notifyAfterAmount(ingredientData.getNotifyAfterAmount())
                .measurementUnit(measurement)
                .build();
    }

}

