package ru.sushi.delivery.kds.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.controller.dto.request.WriteOffRequest;
import ru.sushi.delivery.kds.domain.persist.entity.product.IngredientItem;
import ru.sushi.delivery.kds.domain.persist.entity.product.PrepackItem;
import ru.sushi.delivery.kds.domain.persist.entity.product.SourceItem;
import ru.sushi.delivery.kds.domain.persist.entity.product.WriteOffItem;
import ru.sushi.delivery.kds.domain.persist.repository.WriteOffItemRepository;
import ru.sushi.delivery.kds.domain.persist.repository.product.IngredientItemRepository;
import ru.sushi.delivery.kds.domain.persist.repository.product.PrepackItemRepository;
import ru.sushi.delivery.kds.dto.WriteOffItemDto;
import ru.sushi.delivery.kds.model.DiscontinuedReason;
import ru.sushi.delivery.kds.model.SourceType;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WriteOffService {

    private final IngredientItemService ingredientItemService;
    private final PrepackItemService prepackItemService;
    private final SourceService sourceService;
    private final WriteOffItemRepository writeOffItemRepository;
    private final IngredientItemRepository ingredientItemRepository;
    private final PrepackItemRepository prepackItemRepository;

    public Page<WriteOffItemDto> getAll(PageRequest pageRequest) {
        return writeOffItemRepository.findAllWithName(pageRequest).map(WriteOffItemDto::of);
    }

    public void writeOff(WriteOffRequest writeOffRequest) {
        if (writeOffRequest.getSourceType() == SourceType.INGREDIENT) {

            IngredientItem ingredientItem = ingredientItemService.get(writeOffRequest.getSourceItemId());

            if (writeOffRequest.getDiscontinuedReason() == DiscontinuedReason.OTHER) {
                this.writeOffItemWithOtherReason(
                        ingredientItem,
                        writeOffRequest,
                        ingredientItem.getIngredient().getId()
                );
            }
            else if (writeOffRequest.getDiscontinuedReason() == DiscontinuedReason.SPOILED) {
                this.writeOffSpoiledItem(
                        ingredientItem,
                        writeOffRequest,
                        ingredientItem.getIngredient().getId()
                );
            }
        }
        else if (writeOffRequest.getSourceType() == SourceType.PREPACK) {
            PrepackItem prepackItem = prepackItemService.get(writeOffRequest.getSourceItemId());

            if (writeOffRequest.getDiscontinuedReason() == DiscontinuedReason.OTHER) {
                this.writeOffItemWithOtherReason(
                        prepackItem,
                        writeOffRequest,
                        prepackItem.getPrepack().getId()
                );
            }
            else if (writeOffRequest.getDiscontinuedReason() == DiscontinuedReason.SPOILED) {
                this.writeOffSpoiledItem(
                        prepackItem,
                        writeOffRequest,
                        prepackItem.getPrepack().getId()
                );
            }
        }
    }

    public void writeOffSpoiledItem(SourceItem sourceItem, WriteOffRequest writeOffRequest, Long productId) {
        if (sourceItem.getExpirationDate().isAfter(Instant.now())) {
            log.error("Продукт с id {} не просрочен, списание невозможно", productId);
            throw new IllegalArgumentException("Невозможно списать. Продукт еще не просрочен.");
        }

        Double newAmount = sourceItem.getAmount() - writeOffRequest.getWriteOffAmount();
        sourceItem.setAmount(newAmount);
        Boolean isCompleted = newAmount >= 0;

        String comment = createComment(sourceItem, writeOffRequest, "'продукт испорчен'");

        WriteOffItem writeOffItem = WriteOffItem.of(
                sourceItem,
                writeOffRequest.getWriteOffAmount(),
                isCompleted,
                comment,
                writeOffRequest.getEmployeeName(),
                writeOffRequest.getDiscontinuedReason()
        );
        writeOffItemRepository.save(writeOffItem);

        if (isCompleted) {
            sourceService.updateSourceItemAmount(sourceItem);
        }
    }

    public void writeOffItemWithOtherReason(SourceItem sourceItem, WriteOffRequest writeOffRequest, Long productId) {

        Double newAmount = sourceItem.getAmount() - writeOffRequest.getWriteOffAmount();
        sourceItem.setAmount(newAmount);
        Boolean isCompleted = newAmount >= 0;

        String comment = createComment(sourceItem, writeOffRequest, writeOffRequest.getCustomReasonComment());

        WriteOffItem writeOffItem = WriteOffItem.of(
                sourceItem,
                writeOffRequest.getWriteOffAmount(),
                isCompleted,
                comment,
                writeOffRequest.getEmployeeName(),
                writeOffRequest.getDiscontinuedReason()
        );
        writeOffItemRepository.save(writeOffItem);

        if (isCompleted) {
            sourceService.updateSourceItemAmount(sourceItem);
        }
    }

    private String createComment(SourceItem sourceItem, WriteOffRequest writeOffRequest, String reason) {
        return String.format(
                "%s '%s' был списан сотрудником %s по причине '%s'",
                sourceItem.getSourceType().getValue(),
                sourceService.getSourceItemName(sourceItem),
                writeOffRequest.getEmployeeName(),
                reason
        );
    }
}

