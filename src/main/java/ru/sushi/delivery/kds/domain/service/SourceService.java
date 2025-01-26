package ru.sushi.delivery.kds.domain.service;

import com.vaadin.flow.router.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.controller.dto.SourceDto;
import ru.sushi.delivery.kds.domain.persist.entity.Measurement;
import ru.sushi.delivery.kds.domain.persist.entity.product.IngredientItem;
import ru.sushi.delivery.kds.domain.persist.entity.product.PrepackItem;
import ru.sushi.delivery.kds.domain.persist.entity.product.Product;
import ru.sushi.delivery.kds.domain.persist.entity.product.SourceItem;
import ru.sushi.delivery.kds.domain.persist.repository.product.IngredientItemRepository;
import ru.sushi.delivery.kds.domain.persist.repository.product.IngredientRepository;
import ru.sushi.delivery.kds.domain.persist.repository.product.PrepackItemRepository;
import ru.sushi.delivery.kds.domain.persist.repository.product.PrepackRepository;
import ru.sushi.delivery.kds.model.SourceType;

import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class SourceService {

    private final IngredientItemRepository ingredientItemRepository;
    private final IngredientRepository ingredientRepository;
    private final PrepackItemRepository prepackItemRepository;
    private final PrepackRepository prepackRepository;

    public List<SourceDto> getAllSources() {
        return Stream.concat(
                this.prepackRepository.findAll().stream()
                        .map(prepack -> new SourceDto(
                                prepack.getId(),
                                prepack.getName(),
                                SourceType.PREPACK.name()
                        )),
                this.ingredientRepository.findAll().stream()
                        .map(ingredient -> new SourceDto(
                                ingredient.getId(),
                                ingredient.getName(),
                                SourceType.INGREDIENT.name()
                        ))
        ).toList();
    }

    public List<SourceDto> getAllPrepacks() {
        return this.prepackRepository.findAll().stream()
                .map(prepack -> new SourceDto(
                        prepack.getId(),
                        prepack.getName(),
                        SourceType.PREPACK.name()
                )).toList();
    }

    public List<SourceItem> getSourceActiveItems(Long sourceId, SourceType sourceType) {
        return switch (sourceType) {
            case INGREDIENT -> this.ingredientItemRepository.findActiveByIngredientId(sourceId);
            case PREPACK -> this.prepackItemRepository.findActiveByPrepackId(sourceId);
            default -> throw new IllegalStateException("Unexpected value: " + sourceType);
        };
    }

    public String getSourceItemName(SourceItem sourceItem) {
        return switch (sourceItem.getSourceType()) {
            case INGREDIENT -> ((IngredientItem) sourceItem).getIngredient().getName();
            case PREPACK -> ((PrepackItem) sourceItem).getPrepack().getName();
            default -> throw new IllegalStateException("Unexpected value: " + sourceItem.getSourceType());
        };
    }

    public Measurement getSourceItemMeasurementUnit(Long sourceId, SourceType sourceType) {
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

    public String getSourceItemName(Long sourceId, SourceType sourceType) {
        return switch (sourceType) {
            case INGREDIENT -> this.ingredientRepository.findById(sourceId).map(Product::getName)
                    .orElseThrow(() -> new NotFoundException("Ingredient not found by id " + sourceId));
            case PREPACK -> this.prepackRepository.findById(sourceId).map(Product::getName)
                    .orElseThrow(() -> new NotFoundException("Prepack not found by " + sourceId));
            default -> throw new IllegalStateException("Unexpected value: " + sourceType);
        };
    }
}
