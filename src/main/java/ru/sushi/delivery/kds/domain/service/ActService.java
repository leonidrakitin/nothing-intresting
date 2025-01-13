package ru.sushi.delivery.kds.domain.service;

import com.vaadin.flow.router.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.controller.dto.InvoiceActDto;
import ru.sushi.delivery.kds.domain.controller.dto.PrepackRecipeItemDto;
import ru.sushi.delivery.kds.domain.controller.dto.ProcessingActDto;
import ru.sushi.delivery.kds.domain.controller.dto.request.GetInvoicesRequest;
import ru.sushi.delivery.kds.domain.controller.dto.response.GetInvoicesResponse;
import ru.sushi.delivery.kds.domain.persist.entity.act.InvoiceAct;
import ru.sushi.delivery.kds.domain.persist.entity.act.InvoiceActItem;
import ru.sushi.delivery.kds.domain.persist.entity.act.ProcessingAct;
import ru.sushi.delivery.kds.domain.persist.entity.act.ProcessingSourceItem;
import ru.sushi.delivery.kds.domain.persist.entity.product.Ingredient;
import ru.sushi.delivery.kds.domain.persist.entity.product.IngredientItem;
import ru.sushi.delivery.kds.domain.persist.entity.product.Prepack;
import ru.sushi.delivery.kds.domain.persist.entity.product.PrepackItem;
import ru.sushi.delivery.kds.domain.persist.repository.act.InvoiceActItemRepository;
import ru.sushi.delivery.kds.domain.persist.repository.act.InvoiceActRepository;
import ru.sushi.delivery.kds.domain.persist.repository.act.ProcessingActRepository;
import ru.sushi.delivery.kds.domain.persist.repository.act.ProcessingSourceItemRepository;
import ru.sushi.delivery.kds.domain.persist.repository.product.IngredientItemRepository;
import ru.sushi.delivery.kds.domain.persist.repository.product.PrepackItemRepository;
import ru.sushi.delivery.kds.dto.act.InvoiceActItemDto;
import ru.sushi.delivery.kds.model.SourceType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class ActService {

    private final InvoiceActRepository invoiceActRepository;
    private final InvoiceActItemRepository invoiceActItemRepository;
    private final IngredientItemRepository ingredientItemRepository;
    private final IngredientService ingredientService;
    private final PrepackItemRepository prepackItemRepository;
    private final PrepackService prepackService;
    private final ProcessingActRepository processingActRepository;
    private final ProcessingSourceItemRepository processingSourceItemRepository;
    private final RecipeService recipeService;
    private final SourceService sourceService;

    @Transactional
    public List<GetInvoicesResponse> getAllInvoices(GetInvoicesRequest request) {
        final Sort.Order order = new Sort.Order(
                Sort.Direction.fromString(request.getSortDirection()),
                request.getFieldSort()
        );
        return this.invoiceActRepository.findFiltered(
                        request.getVendorFilter(),
//                        request.getFromDate() == null ? null : LocalDateTime.of(request.getFromDate(), LocalTime.MIN),
                        PageRequest.of(request.getPageNumber(), request.getPageSize(), Sort.by(order))
                ).stream()
                .map(GetInvoicesResponse::of)
                .toList();
    }

    @Transactional
    public InvoiceActDto getInvoice(long invoiceId) {
        return this.invoiceActRepository.findById(invoiceId)
                .map(this::buildInvoiceActDto)
                .orElseThrow(() -> new NotFoundException("Invoice not found by id " + invoiceId));
    }

    @Transactional
    public void deleteInvoiceAct(long invoiceId) {
        // todo delete ingredients/prepack items that connected
        this.invoiceActItemRepository.deleteByInvoiceActId(invoiceId);
        this.invoiceActRepository.deleteById(invoiceId);
    }

    @Transactional
    public void saveInvoiceAct(InvoiceActDto invoiceData) {
        List<InvoiceActItem> invoiceActItems = new ArrayList<>();
        List<IngredientItem> ingredientItems = new ArrayList<>();
        List<PrepackItem> prepackItems = new ArrayList<>();

        InvoiceAct invoiceAct = InvoiceAct.of(invoiceData);
        this.invoiceActRepository.save(invoiceAct);

        Set<Long> currentActsItemIds = new HashSet<>(
                this.invoiceActItemRepository.findAllByInvoiceActId(invoiceAct.getId())
                        .stream()
                        .map(InvoiceActItem::getId)
                        .toList()
        );

        if (invoiceData.getItemDataList().isEmpty()) {
            throw new IllegalArgumentException("Item data is empty");
        }

        for (InvoiceActItemDto itemActData : invoiceData.getItemDataList()) {
            SourceType sourceType = SourceType.valueOf(itemActData.getSourceType());
            InvoiceActItem itemAct = InvoiceActItem.of(sourceType, invoiceAct, itemActData);
            invoiceActItems.add(itemAct);
            if (sourceType == SourceType.INGREDIENT) {
                IngredientItem ingredientItem = Optional.ofNullable(itemActData.getId())
                        .map(this.ingredientItemRepository::findById)
                        .flatMap(Function.identity())
                        .orElseGet(() -> {
                            Ingredient ingredient = this.ingredientService.get(itemActData.getSourceId());
                            return IngredientItem.of(ingredient, itemActData, itemAct);
                        });
                ingredientItems.add(ingredientItem);
            } else if (sourceType == SourceType.PREPACK) {
                PrepackItem prepackItem = Optional.ofNullable(itemActData.getId())
                        .map(this.prepackItemRepository::findById)
                        .flatMap(Function.identity())
                        .orElseGet(() -> {
                            Prepack prepack = this.prepackService.get(itemActData.getSourceId());
                            return PrepackItem.of(prepack, itemActData, itemAct);
                        });
                prepackItems.add(prepackItem);
            } else {
                throw new UnsupportedOperationException("Unsupported source type: " + itemActData.getSourceType());
            }
            currentActsItemIds.remove(itemActData.getId());
        }
        this.invoiceActItemRepository.saveAll(invoiceActItems);
        this.ingredientItemRepository.saveAll(ingredientItems);
        this.prepackItemRepository.saveAll(prepackItems);

        this.invoiceActItemRepository.deleteAllById(currentActsItemIds);
        // todo delete ingredients/prepack items that connected
    }

    @Transactional
    public void createProcessingAct(ProcessingActDto processingData) {
        Prepack targetPrepack = this.prepackService.get(processingData.getPrepackId());
        ProcessingAct processingAct = ProcessingAct.of(targetPrepack, processingData);
        this.processingActRepository.save(processingAct);
        PrepackItem prepackItem = PrepackItem.of(targetPrepack, processingData, processingAct);
        this.prepackItemRepository.save(prepackItem);

        List<ProcessingSourceItem> processingSourceItems = new ArrayList<>();
        for (PrepackRecipeItemDto item : processingData.getItemDataList()) {
            SourceType sourceType = SourceType.valueOf(item.getSourceType());
            processingSourceItems.add(ProcessingSourceItem.of(sourceType, processingAct, item));
            this.recipeService.writeOffSourceItems(
                    item.getFinalAmount(),
                    item.getSourceId(),
                    SourceType.valueOf(item.getSourceType())
            );
        }
        this.processingSourceItemRepository.saveAll(processingSourceItems);
    }

    private InvoiceActDto buildInvoiceActDto(InvoiceAct invoiceAct) {
        return new InvoiceActDto(
                invoiceAct.getId(),
                invoiceAct.getEmployeeId(),
                invoiceAct.getName(),
                invoiceAct.getVendor(),
                invoiceAct.getDate().toLocalDate(),
                invoiceAct.getInvoiceActItems().stream().map(this::buildInvoiceActItemDto).toList()
        );
    }

    private InvoiceActItemDto buildInvoiceActItemDto(InvoiceActItem invoiceItem) {
        return new InvoiceActItemDto(
                invoiceItem.getId(),
                this.sourceService.getSourceItemName(invoiceItem.getSourceId(), invoiceItem.getSourceType()),
                invoiceItem.getSourceId(),
                invoiceItem.getSourceType().name(),
                invoiceItem.getAmount(),
                invoiceItem.getPrice(),
                null
        );
    }
}
