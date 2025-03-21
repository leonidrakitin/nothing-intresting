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
import java.util.List;

@Route(value = "invoices/:id")
public class InvoiceAddView extends VerticalLayout implements BeforeEnterObserver {

    private static final String NEW_INVOICE = "new";

    private final ActService actService;
    private final SourceItemService sourceItemService;
    private InvoiceActDto invoice;
    private boolean isEditMode;
    private final List<SourceDto> sources;
    private Grid<InvoiceActItemDto> itemsGrid;
    private Span summarySpan;

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

        // Инициализация таблицы для позиций
        itemsGrid = new Grid<>(InvoiceActItemDto.class, false);
        itemsGrid.setItems(invoice.getItemDataList());
        itemsGrid.setHeight("400px"); // Устанавливаем фиксированную высоту для прокрутки

        // Колонки таблицы
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
            })).setHeader("Наименование").setWidth("40%");

            itemsGrid.addColumn(new ComponentRenderer<>(item -> {
                NumberField amountField = new NumberField();
                amountField.setValue(item.getAmount());
                amountField.addValueChangeListener(e -> {
                    InvoiceActItemDto updatedItem = item.toBuilder().amount(e.getValue()).build();
                    updateItemInList(item, updatedItem);
                    updateGridAndSummary();
                });
                return amountField;
            })).setHeader("Количество").setWidth("15%");

            itemsGrid.addColumn(new ComponentRenderer<>(item -> {
                NumberField priceField = new NumberField();
                priceField.setValue(item.getPrice());
                priceField.addValueChangeListener(e -> {
                    InvoiceActItemDto updatedItem = item.toBuilder().price(e.getValue()).build();
                    updateItemInList(item, updatedItem);
                    updateGridAndSummary();
                });
                return priceField;
            })).setHeader("Цена").setWidth("15%");

            itemsGrid.addColumn(item -> String.format("%.2f", item.getAmount() * item.getPrice()))
                .setHeader("Сумма").setWidth("15%");

            itemsGrid.addColumn(new ComponentRenderer<>(item -> {
                Button removeButton = new Button("Удалить", e -> {
                    List<InvoiceActItemDto> newList = new ArrayList<>(invoice.getItemDataList());
                    newList.remove(item);
                    invoice = invoice.toBuilder().itemDataList(newList).build();
                    updateGridAndSummary();
                });
                return removeButton;
            })).setHeader("Действия").setWidth("15%");
        }
        else {
            itemsGrid.addColumn(item -> item.getName() != null ? item.getName() : "")
                .setHeader("Наименование").setWidth("40%");
            itemsGrid.addColumn(InvoiceActItemDto::getAmount).setHeader("Количество").setWidth("15%");
            itemsGrid.addColumn(InvoiceActItemDto::getPrice).setHeader("Цена").setWidth("15%");
            itemsGrid.addColumn(item -> String.format("%.2f", item.getAmount() * item.getPrice()))
                .setHeader("Сумма").setWidth("30%");
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
            newList.set(index, newItem);
            invoice = invoice.toBuilder().itemDataList(newList).build();
        }
    }

    private void updateGridAndSummary() {
        itemsGrid.setItems(invoice.getItemDataList());
        updateSummary();
    }

    private void updateSummary() {
        double totalCost = invoice.getItemDataList().stream()
            .mapToDouble(item -> item.getAmount() * item.getPrice())
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