package ru.sushi.delivery.kds.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.controller.dto.IngredientItemData;
import ru.sushi.delivery.kds.domain.persist.entity.product.Ingredient;
import ru.sushi.delivery.kds.domain.persist.entity.product.IngredientItem;
import ru.sushi.delivery.kds.domain.persist.entity.product.SourceItem;
import ru.sushi.delivery.kds.domain.persist.repository.product.IngredientItemRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IngredientItemService {

    private final IngredientItemRepository ingredientItemRepository;

    public IngredientItem get(Long id) {
        return ingredientItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("IngredientItem not found by id " + id));
    }

    public boolean checkIfAlmostEmpty(Ingredient ingredient) {
        double amount = this.ingredientItemRepository.findActiveByIngredientId(ingredient.getId()).stream()
                .map(SourceItem::getAmount)
                .mapToDouble(Double::doubleValue)
                .sum();
        return amount <= ingredient.getNotifyAfterAmount();
    }

    public List<IngredientItemData> getGetAll() {
        return ingredientItemRepository.findAll().stream()
                .map(IngredientItemData::of)
                .toList();
    }
}
