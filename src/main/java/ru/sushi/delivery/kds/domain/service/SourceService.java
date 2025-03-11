package ru.sushi.delivery.kds.domain.service;

import com.vaadin.flow.router.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.persist.entity.Measurement;
import ru.sushi.delivery.kds.domain.persist.entity.product.Product;
import ru.sushi.delivery.kds.domain.persist.repository.product.IngredientRepository;
import ru.sushi.delivery.kds.domain.persist.repository.product.PrepackRepository;
import ru.sushi.delivery.kds.model.SourceType;

@Service
@RequiredArgsConstructor
public class SourceService {

    private final IngredientRepository ingredientRepository;
    private final PrepackRepository prepackRepository;

    public Measurement getSourceMeasurementUnit(Long sourceId, SourceType sourceType) {
        return switch (sourceType) {
            case INGREDIENT -> this.ingredientRepository.findById(sourceId)
                    .orElseThrow(NotFoundException::new)
                    .getMeasurementUnit();
            case PREPACK -> this.prepackRepository.findById(sourceId)
                    .orElseThrow(NotFoundException::new)
                    .getMeasurementUnit();
            default -> throw new IllegalStateException("Unexpected value: " + sourceType);
        };
    }

    public String getSourceName(Long sourceId, SourceType sourceType) {
        return switch (sourceType) {
            case INGREDIENT -> this.ingredientRepository.findById(sourceId).map(Product::getName)
                    .orElseThrow(() -> new NotFoundException("Ingredient not found by id " + sourceId));
            case PREPACK -> this.prepackRepository.findById(sourceId).map(Product::getName)
                    .orElseThrow(() -> new NotFoundException("Prepack not found by " + sourceId));
            default -> throw new IllegalStateException("Unexpected value: " + sourceType);
        };
    }

}
