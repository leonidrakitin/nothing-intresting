package ru.sushi.delivery.kds.domain.service;

import com.vaadin.flow.router.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.controller.dto.InvoiceActDto;
import ru.sushi.delivery.kds.domain.controller.dto.request.GetInvoicesRequest;
import ru.sushi.delivery.kds.domain.controller.dto.response.GetInvoicesResponse;
import ru.sushi.delivery.kds.domain.persist.entity.Employee;
import ru.sushi.delivery.kds.domain.persist.entity.Measurement;
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
import ru.sushi.delivery.kds.dto.act.ProcessingActDto;
import ru.sushi.delivery.kds.dto.act.ProcessingSourceItemDto;
import ru.sushi.delivery.kds.model.SourceType;

import java.time.Instant;
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
    private final IngredientItemService ingredientItemService;
    private final IngredientService ingredientService;
    private final PrepackItemRepository prepackItemRepository;
    private final PrepackService prepackService;
    private final PrepackItemService prepackItemService;
    private final ProcessingActRepository processingActRepository;
    private final ProcessingSourceItemRepository processingSourceItemRepository;
    private final EmployeeService employeeService;

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
                .map(InvoiceActDto::of)
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
        List<ProcessingSourceItem> processingSourceItems = new ArrayList<>();
        List<IngredientItem> ingredientItems = new ArrayList<>();
        List<PrepackItem> prepackItems = new ArrayList<>();

        Employee employee = this.employeeService.get(processingData.getEmployeeId());

        Prepack targetPrepack = this.prepackService.get(processingData.getPrepackId());
        PrepackItem targetPrepackItem = PrepackItem.of(targetPrepack, processingData);

        ProcessingAct processingAct = ProcessingAct.of(targetPrepack, processingData);

        for (ProcessingSourceItemDto item : processingData.getItemDataList()) {
            processingSourceItems.add(ProcessingSourceItem.of(processingAct, item));
            if (item.getSourceType() == SourceType.INGREDIENT) {
                IngredientItem ingredientItem = this.ingredientItemService.get(item.getSourceId());
                double balance = this.calculateBalanceIfPossible(
                        ingredientItem.getAmount(),
                        processingAct,
                        ingredientItem.getIngredient().getName(),
                        ingredientItem.getIngredient().getMeasurementUnit()
                );
                ingredientItems.add(this.updateIngredientItem(ingredientItem, balance, employee));
            } else if (item.getSourceType() == SourceType.PREPACK) {
                PrepackItem prepackItem = this.prepackItemService.get(item.getSourceId());
                double balance = this.calculateBalanceIfPossible(
                        prepackItem.getAmount(),
                        processingAct,
                        prepackItem.getPrepack().getName(),
                        prepackItem.getPrepack().getMeasurementUnit()
                );
                prepackItems.add(this.updatePrepackItem(prepackItem, balance, employee));
            } else {
                throw new UnsupportedOperationException("Unsupported source type: " + item.getSourceType());
            }
        }
        this.prepackItemRepository.save(targetPrepackItem);
        this.processingActRepository.save(processingAct);
        this.processingSourceItemRepository.saveAll(processingSourceItems);
        this.ingredientItemRepository.saveAll(ingredientItems);
        this.prepackItemRepository.saveAll(prepackItems);
    }

    private double calculateBalanceIfPossible(
            Double itemAmount,
            ProcessingAct processingData,
            String name,
            Measurement measurement
    ) {
        double balance = itemAmount - processingData.getAmount();
        if (balance < 0) {
            throw new IllegalArgumentException(String.format(
                    "There is not enough '%s' in warehouse, it's %.1f%s", name, itemAmount, measurement.getName()
            ));
        }
        return balance;
    }

    private IngredientItem updateIngredientItem(IngredientItem ingredientItem, double balance, Employee employee) {
        return ingredientItem.toBuilder()
                .amount(balance)
                .updatedAt(Instant.now())
                .updatedBy(employee.getName())
                .build();
    }

    private PrepackItem updatePrepackItem(PrepackItem prepackItem, double balance, Employee employee) {
        return prepackItem.toBuilder()
                .amount(balance)
                .updatedAt(Instant.now())
                .updatedBy(employee.getName())
                .build();
    }
}
