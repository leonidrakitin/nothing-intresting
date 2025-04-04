package ru.sushi.delivery.kds.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.controller.dto.SourceDto;
import ru.sushi.delivery.kds.domain.persist.entity.product.IngredientItem;
import ru.sushi.delivery.kds.domain.persist.entity.product.PrepackItem;
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
public class SourceItemService {

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

    public void updateSourceItemAmount(SourceItem sourceItem) {
        if (sourceItem instanceof IngredientItem ingredientItem) {
            ingredientItemRepository.save(ingredientItem);
        }
        else if (sourceItem instanceof PrepackItem prepackItem) {
            prepackItemRepository.save(prepackItem);
        }
    }
}
