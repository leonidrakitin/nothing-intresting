package ru.sushi.delivery.kds.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import ru.sushi.delivery.kds.domain.controller.dto.InvoiceActDto;
import ru.sushi.delivery.kds.domain.controller.dto.SourceDto;
import ru.sushi.delivery.kds.domain.service.ActService;
import ru.sushi.delivery.kds.domain.service.SourceItemService;
import ru.sushi.delivery.kds.dto.act.InvoiceActItemDto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Route(value = "invoice-add/:id")
public class InvoiceAddView extends VerticalLayout implements BeforeEnterObserver {

    private static final String NEW_INVOICE = "new";

    private final ActService actService;
    private final SourceItemService sourceItemService;
    private InvoiceActDto invoice;
    private boolean isEditMode;
    private final List<SourceDto> sources;
    private Grid<InvoiceActItemDto> itemsGrid;
    private Span summarySpan;
    private final Map<InvoiceActItemDto, Double> itemVatMap = new HashMap<>();

    public InvoiceAddView(ActService actService, SourceItemService sourceItemService) {
        this.actService = actService;
        this.sourceItemService = sourceItemService;
        this.sources = sourceItemService.getAllSources();
        setSizeFull();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String idParam = event.getRouteParameters().get("id").orElse(NEW_INVOICE);
        if (NEW_INVOICE.equals(idParam)) {
            invoice = InvoiceActDto.builder()
                .date(LocalDate.now())
                .vendor("")
                .itemDataList(new ArrayList<>())
                .build();
            isEditMode = true;
        }
        else {
            Long invoiceId = Long.parseLong(idParam);
            invoice = actService.getInvoice(invoiceId);
            isEditMode = false;
            invoice.getItemDataList().forEach(item -> itemVatMap.put(item, 0.0));
        }
        buildUI();
    }

    private void buildUI() {
        removeAll();

        DatePicker dateField = new DatePicker("Дата");
        dateField.setValue(invoice.getDate());
        dateField.setReadOnly(!isEditMode);
        if (isEditMode) {
            dateField.addValueChangeListener(e -> invoice = invoice.toBuilder().date(e.getValue()).build());
        }

        TextField vendorField = new TextField("Поставщик");
        vendorField.setValue(invoice.getVendor() != null ? invoice.getVendor() : "");
        vendorField.setReadOnly(!isEditMode);
        if (isEditMode) {
            vendorField.addValueChangeListener(e -> invoice = invoice.toBuilder().vendor(e.getValue()).build());
        }

        itemsGrid = new Grid<>(InvoiceActItemDto.class, false);
        itemsGrid.setItems(invoice.getItemDataList());
        itemsGrid.setHeight("400px");

        if (isEditMode) {
            itemsGrid.addColumn(new ComponentRenderer<>(item -> {
                ComboBox<SourceDto> sourceCombo = new ComboBox<>();
                sourceCombo.setItems(sources);
                sourceCombo.setItemLabelGenerator(source -> source.getName() + " (" + source.getType() + ")");
                sourceCombo.setValue(findSource(item.getSourceId(), item.getSourceType()));
                sourceCombo.addValueChangeListener(e -> {
                    SourceDto selected = e.getValue();
                    if (selected != null) {
                        InvoiceActItemDto updatedItem = item.toBuilder()
                            .sourceId(selected.getId())
                            .sourceType(selected.getType())
                            .name(selected.getName())
                            .build();
                        updateItemInList(item, updatedItem);
                        updateGridAndSummary();
                    }
                });
                return sourceCombo;
            })).setHeader("Наименование").setWidth("30%");

            itemsGrid.addColumn(new ComponentRenderer<>(item -> {
                NumberField amountField = new NumberField();
                amountField.setValue(item.getAmount());
                amountField.addValueChangeListener(e -> {
                    double newAmount = e.getValue();
                    if (newAmount > 0) {
                        double vat = itemVatMap.getOrDefault(item, 0.0) / 100;
                        double currentTotalWithVat = Math.round(item.getAmount() * item.getPrice() * (1 + vat) * 100.0) / 100.0;
                        double newPrice = Math.round((currentTotalWithVat / (1 + vat) / newAmount) * 100.0) / 100.0;
                        InvoiceActItemDto updatedItem = item.toBuilder()
                            .amount(newAmount)
                            .price(newPrice)
                            .build();
                        updateItemInList(item, updatedItem);
                        updateGridAndSummary();
                    }
                    else {
                        Notification.show("Количество должно быть больше нуля");
                        amountField.setValue(item.getAmount());
                    }
                });
                return amountField;
            })).setHeader("Количество").setWidth("15%");

            itemsGrid.addColumn(item -> String.format("%.2f", item.getPrice()))
                .setHeader("Цена").setWidth("15%");

            itemsGrid.addColumn(new ComponentRenderer<>(item -> {
                double vat = itemVatMap.getOrDefault(item, 0.0) / 100;
                double totalWithVat = Math.round(item.getAmount() * item.getPrice() * (1 + vat) * 100.0) / 100.0;
                NumberField totalField = new NumberField();
                totalField.setValue(totalWithVat);
                totalField.addValueChangeListener(e -> {
                    double newTotalWithVat = e.getValue();
                    double amount = item.getAmount();
                    if (amount > 0) {
                        double itemVat = itemVatMap.getOrDefault(item, 0.0) / 100;
                        double newPrice = Math.round((newTotalWithVat / (1 + itemVat) / amount) * 100.0) / 100.0;
                        InvoiceActItemDto updatedItem = item.toBuilder()
                            .price(newPrice)
                            .build();
                        updateItemInList(item, updatedItem);
                        updateGridAndSummary();
                    }
                    else {
                        Notification.show("Количество должно быть больше нуля");
                    }
                });
                return totalField;
            })).setHeader("Сумма").setWidth("15%");

            itemsGrid.addColumn(new ComponentRenderer<>(item -> {
                NumberField vatField = new NumberField();
                vatField.setValue(itemVatMap.getOrDefault(item, 0.0));
                vatField.addValueChangeListener(e -> {
                    double newVat = e.getValue() / 100; // НДС в долях
                    double currentTotalWithVat = Math.round(item.getAmount() * item.getPrice() * (1 + itemVatMap.getOrDefault(item, 0.0) / 100) * 100.0) / 100.0;
                    double newPrice = Math.round((currentTotalWithVat / (item.getAmount() * (1 + newVat))) * 100.0) / 100.0;
                    InvoiceActItemDto updatedItem = item.toBuilder().price(newPrice).build();
                    updateItemInList(item, updatedItem);
                    itemVatMap.put(updatedItem, e.getValue());
                    updateGridAndSummary();
                });
                return vatField;
            })).setHeader("НДС %").setWidth("10%");

            itemsGrid.addColumn(new ComponentRenderer<>(item -> {
                Button removeButton = new Button("Удалить", e -> {
                    List<InvoiceActItemDto> newList = new ArrayList<>(invoice.getItemDataList());
                    newList.remove(item);
                    invoice = invoice.toBuilder().itemDataList(newList).build();
                    itemVatMap.remove(item);
                    updateGridAndSummary();
                });
                return removeButton;
            })).setHeader("Действия").setWidth("15%");
        }
        else {
            itemsGrid.addColumn(item -> item.getName() != null ? item.getName() : "")
                .setHeader("Наименование").setWidth("30%");
            itemsGrid.addColumn(item -> String.format("%.2f%%", itemVatMap.getOrDefault(item, 0.0)))
                .setHeader("НДС").setWidth("10%");
            itemsGrid.addColumn(InvoiceActItemDto::getAmount).setHeader("Количество").setWidth("15%");
            itemsGrid.addColumn(item -> {
                double vat = itemVatMap.getOrDefault(item, 0.0) / 100;
                return String.format("%.2f", Math.round(item.getAmount() * item.getPrice() * (1 + vat) * 100.0) / 100.0);
            }).setHeader("Сумма").setWidth("15%");
            itemsGrid.addColumn(item -> String.format("%.2f", item.getPrice()))
                .setHeader("Цена").setWidth("15%");
        }

        Button addItemButton = new Button("Добавить позицию", e -> {
            InvoiceActItemDto newItem = InvoiceActItemDto.builder()
                .name("")
                .sourceId(null)
                .sourceType("")
                .amount(0.0)
                .price(0.0)
                .build();
            List<InvoiceActItemDto> newList = new ArrayList<>(invoice.getItemDataList());
            newList.add(newItem);
            invoice = invoice.toBuilder().itemDataList(newList).build();
            itemVatMap.put(newItem, 0.0);
            updateGridAndSummary();
        });
        addItemButton.setVisible(isEditMode);

        summarySpan = new Span();
        updateSummary();

        Button actionButton = isEditMode
            ? new Button("Сохранить", e -> saveInvoice())
            : new Button("Редактировать", e -> {
            isEditMode = true;
            buildUI();
        });

        add(dateField, vendorField, itemsGrid, addItemButton, summarySpan, actionButton);
    }

    private SourceDto findSource(Long sourceId, String sourceType) {
        return sources.stream()
            .filter(s -> s.getId().equals(sourceId) && s.getType().equals(sourceType))
            .findFirst()
            .orElse(null);
    }

    private void updateItemInList(InvoiceActItemDto oldItem, InvoiceActItemDto newItem) {
        List<InvoiceActItemDto> newList = new ArrayList<>(invoice.getItemDataList());
        int index = newList.indexOf(oldItem);
        if (index != -1) {
            Double vat = itemVatMap.get(oldItem);
            newList.set(index, newItem);
            invoice = invoice.toBuilder().itemDataList(newList).build();
            itemVatMap.remove(oldItem);
            itemVatMap.put(newItem, vat != null ? vat : 0.0);
        }
    }

    private void updateGridAndSummary() {
        itemsGrid.setItems(invoice.getItemDataList());
        updateSummary();
    }

    private void updateSummary() {
        double totalCost = invoice.getItemDataList().stream()
            .mapToDouble(item -> {
                double vat = itemVatMap.getOrDefault(item, 0.0) / 100;
                return Math.round(item.getAmount() * item.getPrice() * (1 + vat) * 100.0) / 100.0;
            })
            .sum();
        int totalItems = invoice.getItemDataList().size();
        summarySpan.setText(String.format("Позиций: %d, Сумма: %.2f", totalItems, totalCost));
    }

    private void saveInvoice() {
        if (invoice.getItemDataList().isEmpty()) {
            Notification.show("Вы не добавили ни одной записи.");
            return;
        }
        for (InvoiceActItemDto item : invoice.getItemDataList()) {
            if (item.getName() == null || item.getName().isEmpty() || item.getAmount() <= 0) {
                Notification.show("Заполните все наименования и количество корректно.");
                return;
            }
        }
        try {
            actService.saveInvoiceAct(invoice);
            Notification.show("Накладная сохранена!");
            UI.getCurrent().navigate(InvoiceListView.class);
        }
        catch (Exception e) {
            Notification.show("Ошибка сохранения накладной: " + e.getMessage());
        }
    }
}