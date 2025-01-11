package ru.sushi.delivery.kds.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
import ru.sushi.delivery.kds.domain.persist.repository.product.IngredientRepository;
import ru.sushi.delivery.kds.domain.persist.repository.product.PrepackItemRepository;
import ru.sushi.delivery.kds.domain.persist.repository.product.PrepackRepository;
import ru.sushi.delivery.kds.dto.act.InvoiceActDto;
import ru.sushi.delivery.kds.dto.act.InvoiceActItemDto;
import ru.sushi.delivery.kds.dto.act.ProcessingActDto;
import ru.sushi.delivery.kds.dto.act.ProcessingSourceItemDto;
import ru.sushi.delivery.kds.model.SourceType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActService {

    private final InvoiceActRepository invoiceActRepository;
    private final InvoiceActItemRepository invoiceActItemRepository;
    private final IngredientRepository ingredientRepository;
    private final IngredientItemRepository ingredientItemRepository;
    private final IngredientItemService ingredientItemService;
    private final IngredientService ingredientService;
    private final PrepackRepository prepackRepository;
    private final PrepackItemRepository prepackItemRepository;
    private final PrepackService prepackService;
    private final PrepackItemService prepackItemService;
    private final ProcessingActRepository processingActRepository;
    private final ProcessingSourceItemRepository processingSourceItemRepository;
    private final EmployeeService employeeService;

    public void createInvoiceAct(InvoiceActDto invoiceData) {
        List<InvoiceActItem> invoiceActItems = new ArrayList<>();
        List<IngredientItem> ingredientItems = new ArrayList<>();
        List<PrepackItem> prepackItems = new ArrayList<>();

        InvoiceAct invoiceAct = InvoiceAct.of(invoiceData);
        this.invoiceActRepository.save(invoiceAct);

        for (InvoiceActItemDto item : invoiceData.getItemDataList()) {
            invoiceActItems.add(InvoiceActItem.of(invoiceAct, item));
            if (item.getSourceType() == SourceType.INGREDIENT) {
                Ingredient ingredient = this.ingredientService.get(item.getSourceId());
                ingredientItems.add(IngredientItem.of(ingredient, item));
            } else if (item.getSourceType() == SourceType.PREPACK) {
                Prepack prepack = this.prepackService.get(item.getSourceId());
                prepackItems.add(PrepackItem.of(prepack, item));
            } else {
                throw new UnsupportedOperationException("Unsupported source type: " + item.getSourceType());
            }
        }
        this.invoiceActItemRepository.saveAll(invoiceActItems);
        this.ingredientItemRepository.saveAll(ingredientItems);
        this.prepackItemRepository.saveAll(prepackItems);
    }

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
