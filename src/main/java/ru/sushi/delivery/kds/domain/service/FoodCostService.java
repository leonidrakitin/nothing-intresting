package ru.sushi.delivery.kds.domain.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.persist.entity.Measurement;
import ru.sushi.delivery.kds.domain.persist.entity.product.Ingredient;
import ru.sushi.delivery.kds.domain.persist.entity.product.MenuItem;
import ru.sushi.delivery.kds.domain.persist.entity.product.Prepack;
import ru.sushi.delivery.kds.domain.persist.entity.product.Product;
import ru.sushi.delivery.kds.domain.persist.entity.recipe.MenuItemRecipe;
import ru.sushi.delivery.kds.domain.persist.entity.recipe.PrepackRecipe;
import ru.sushi.delivery.kds.domain.persist.repository.product.IngredientRepository;
import ru.sushi.delivery.kds.domain.persist.repository.product.MenuItemRepository;
import ru.sushi.delivery.kds.domain.persist.repository.product.PrepackRepository;
import ru.sushi.delivery.kds.domain.persist.repository.recipe.MenuItemRecipeRepository;
import ru.sushi.delivery.kds.domain.persist.repository.recipe.PrepackRecipeRepository;
import ru.sushi.delivery.kds.model.SourceType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FoodCostService {

    private final IngredientRepository ingredientRepository;
    private final PrepackRepository prepackRepository;
    private final MenuItemRepository menuItemRepository;
    private final MenuItemRecipeRepository menuItemRecipeRepository;
    private final PrepackRecipeRepository prepackRecipeRepository;
    private final MenuItemService menuItemService;

    @Transactional
    public void calculateFoodCost() {
        List<Ingredient> ingredients = this.ingredientRepository.findAll();
        List<Prepack> prepacks = this.prepackRepository.findAll();
        List<MenuItem> menuItems = this.menuItemService.getAllMenuItems();
        Map<Long, Ingredient> ingredientsMap = ingredients.stream()
                .collect(Collectors.toUnmodifiableMap(Ingredient::getId, Function.identity()));

        Map<Long, Double> prepackPriceCache = new HashMap<>();
        Map<Long, Double> ingredientPriceCache = ingredients.stream()
                .collect(Collectors.toUnmodifiableMap(
                        Ingredient::getId,
                        i -> Optional.ofNullable(i.getFcPrice()).orElse(0.0)
                ));

        for (Prepack prepack : prepacks) {
            double prepackPrice = this.calculatePrepackFoodCost(prepack, prepackPriceCache, ingredientPriceCache);
            prepack.setFcPrice(prepackPrice);

        }
        for (MenuItem menuItem : menuItems) {
            double fcPrice = 0;
            for (MenuItemRecipe menuItemRecipe : this.menuItemRecipeRepository.findByMenuItemId(menuItem.getId())) {
                double initPrice = 0;
                if (menuItemRecipe.getSourceType() == SourceType.INGREDIENT) {
                    initPrice = ingredientPriceCache.getOrDefault(menuItemRecipe.getSourceId(), 0.0);
                } else {
                    initPrice = prepackPriceCache.getOrDefault(menuItemRecipe.getSourceId(), 0.0);
                }
                boolean isPeaces = menuItemRecipe.getSourceType() == SourceType.INGREDIENT &&
                        Optional.ofNullable(ingredientsMap.get(menuItemRecipe.getSourceId()))
                                .map(Product::getMeasurementUnit)
                                .map(Measurement::getId)
                                .filter(id -> id == 2)
                                .isPresent();
                double measurementCoefValue = 1000;
                if (isPeaces) {
                    measurementCoefValue = 100;
                }
                double recipeFcCost = initPrice / measurementCoefValue * menuItemRecipe.getInitAmount();
                menuItemRecipe.setFcPrice(recipeFcCost);
                fcPrice += recipeFcCost;
            }
            menuItem.setFcPrice(fcPrice);
        }
        this.prepackRepository.saveAll(prepacks);
        this.menuItemRepository.saveAll(menuItems);
    }

    private double calculatePrepackFoodCost(
            Prepack prepack,
            Map<Long, Double> prepackPriceCache,
            Map<Long, Double> ingredientPriceCache
    ) {
        Long prepackId = prepack.getId();
        if (prepackPriceCache.containsKey(prepackId)) {
            return prepackPriceCache.get(prepackId);
        }
        List<PrepackRecipe> prepackRecipeList = this.prepackRecipeRepository.findByPrepackId(prepackId);
        double totalWeight = 0;
//        for (PrepackRecipe prepackRecipe : prepackRecipeList) {
//            if (prepackRecipe.getSourceType() == SourceType.INGREDIENT && prepackRecipe.getMeasurement().getId() == 2) {
//                long onePeaceInGrams = this.ingredientRepository.findById(prepackRecipe.getSourceId())
//                        .map(Ingredient::getPieceInGrams)
//                        .orElse(0L);
//                totalWeight += onePeaceInGrams * prepackRecipe.getFinalAmount();
//            } else {
//                totalWeight += prepackRecipe.getFinalAmount();
//            }
//        }
//        double coefAmount = totalWeight <= 0 ? 1 : 1000 / totalWeight;
        double price = 0;
        for (PrepackRecipe prepackRecipe : prepackRecipeList) {
            if (prepackRecipe.getSourceType() == SourceType.PREPACK) {
                double prepackPriceInit = this.calculatePrepackFoodCost(
                        this.prepackRepository.findById(prepackRecipe.getSourceId()).get(),
                        prepackPriceCache,
                        ingredientPriceCache
                );
                prepackPriceInit = prepackPriceInit * (1 + prepackRecipe.getLossesPercentage());
                double prepackPrice = prepackPriceInit == 0
                        ? 0.0
                        : prepackPriceInit / prepackRecipe.getInitAmount();
                prepack.setFcPrice(prepackPrice);
                prepackPriceCache.put(prepackRecipe.getSourceId(), prepackPrice);
                price += prepackPrice;// * coefAmount;
                prepackRecipe.setFcPrice(prepackPrice);
            } else {
                double ingredientPriceInit = ingredientPriceCache.getOrDefault(prepackRecipe.getSourceId(), 0.0);
                double qtyAmount;
                if (prepackRecipe.getMeasurement().getId() == 2) {
                    qtyAmount = 100;
                } else {
                    qtyAmount = 1000;
                }
                double recipePrice = ingredientPriceInit == 0
                        ? 0.0
                        : ingredientPriceInit * (1 + prepackRecipe.getLossesPercentage()/100) / qtyAmount * prepackRecipe.getInitAmount();
                price += recipePrice;
                prepackRecipe.setFcPrice(recipePrice);
            }
            if (prepackRecipe.getSourceType() == SourceType.INGREDIENT && prepackRecipe.getMeasurement().getId() == 2) {
                long onePeaceInGrams = this.ingredientRepository.findById(prepackRecipe.getSourceId())
                        .map(Ingredient::getPieceInGrams)
                        .orElse(0L);
                totalWeight += onePeaceInGrams * prepackRecipe.getFinalAmount();
            } else {
                totalWeight += prepackRecipe.getInitAmount();
            }
        }
        prepackPriceCache.put(prepack.getId(), price/totalWeight*1000);
        return price;
    }
}
