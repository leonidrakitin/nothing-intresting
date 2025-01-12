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
import java.util.List;
import java.util.Optional;
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
    public void saveInvoiceAct(InvoiceActDto invoiceData) {
        List<InvoiceActItem> invoiceActItems = new ArrayList<>();
        List<IngredientItem> ingredientItems = new ArrayList<>();
        List<PrepackItem> prepackItems = new ArrayList<>();

        InvoiceAct invoiceAct = InvoiceAct.of(invoiceData);
        this.invoiceActRepository.save(invoiceAct);

        if (invoiceData.getItemDataList().isEmpty()) {
            throw new IllegalArgumentException("Item data is empty");
        }

        for (InvoiceActItemDto item : invoiceData.getItemDataList()) {
            SourceType sourceType = SourceType.valueOf(item.getSourceType());
            invoiceActItems.add(InvoiceActItem.of(sourceType, invoiceAct, item));
            if (sourceType == SourceType.INGREDIENT) {
                IngredientItem ingredientItem = Optional.ofNullable(item.getId())
                        .map(this.ingredientItemRepository::findById)
                        .flatMap(Function.identity())
                        .orElseGet(() -> {
                            Ingredient ingredient = this.ingredientService.get(item.getSourceId());
                            return IngredientItem.of(ingredient, item);
                        });
                ingredientItems.add(ingredientItem);
            } else if (sourceType == SourceType.PREPACK) {
                PrepackItem prepackItem = Optional.ofNullable(item.getId())
                        .map(this.prepackItemRepository::findById)
                        .flatMap(Function.identity())
                        .orElseGet(() -> {
                            Prepack prepack = this.prepackService.get(item.getSourceId());
                            return PrepackItem.of(prepack, item);
                        });
                prepackItems.add(prepackItem);
            } else {
                throw new UnsupportedOperationException("Unsupported source type: " + item.getSourceType());
            }
        }
        this.invoiceActItemRepository.saveAll(invoiceActItems);
        this.ingredientItemRepository.saveAll(ingredientItems);
        this.prepackItemRepository.saveAll(prepackItems);
    }

    @Transactional
    public void createProcessingAct(ProcessingActDto processingData) {
        Prepack targetPrepack = this.prepackService.get(processingData.getPrepackId());
        this.prepackItemRepository.save(PrepackItem.of(targetPrepack, processingData));

        ProcessingAct processingAct = ProcessingAct.of(targetPrepack, processingData);
        this.processingActRepository.save(processingAct);

        List<ProcessingSourceItem> processingSourceItems = new ArrayList<>();
        for (PrepackRecipeItemDto item : processingData.getItemDataList()) {
            processingSourceItems.add(ProcessingSourceItem.of(processingAct, item));
            this.recipeService.writeOffSourceItems(item.getFinalAmount(), item.getSourceId(), item.getSourceType());
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
