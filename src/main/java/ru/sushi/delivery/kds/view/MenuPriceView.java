package ru.sushi.delivery.kds.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import ru.sushi.delivery.kds.domain.controller.dto.MenuItemData;
import ru.sushi.delivery.kds.domain.persist.repository.flow.FlowRepository;
import ru.sushi.delivery.kds.domain.service.MenuItemService;

import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.List;

@Route(value = "menu-price")
@PageTitle("Расчет цены | Доставка Суши")
@PermitAll
@CssImport("./styles/menu-price-view.css")
public class MenuPriceView extends VerticalLayout {

    private final DecimalFormat priceFactFormat = new DecimalFormat("#");
    private final DecimalFormat foodCostFormat = new DecimalFormat("#.##");

    private final MenuItemService menuItemService;
    private final FlowRepository flowRepository;

    private final Grid<MenuItemData> menuItemGrid = new Grid<>();
    private final HorizontalLayout summaryLayout = new HorizontalLayout();
    private FormLayout formLayout; // Добавляем как поле класса
    private final NumberField fcCoefField = new NumberField("Коэф себеса (%)");
    private final NumberField priceField = new NumberField("Цена (рублей)");
    private final Button saveButton = new Button("Изменить");
    private final Button cancelButton = new Button("Отменить изменения");
    private MenuItemData currentEditingMenuItem = null;

    @Autowired
    public MenuPriceView(MenuItemService menuItemService, FlowRepository flowRepository) {
        this.menuItemService = menuItemService;
        this.flowRepository = flowRepository;

        setSizeFull();
        summaryLayout.setWidthFull();
        summaryLayout.getStyle().set("margin-bottom", "1em");
        configureGrid();
        add(summaryLayout);
        add(menuItemGrid);
        updateGrid();
    }

    private void configureGrid() {
        menuItemGrid.setSizeFull();
        menuItemGrid.addClassName("menu-price-grid");
        menuItemGrid.setClassNameGenerator(this::rowClassName);

        menuItemGrid.addColumn(MenuItemData::getId)
                .setAutoWidth(true)
                .setHeader("ID")
                .setSortable(true)
                .setClassNameGenerator(item -> "text-center");

        menuItemGrid.addColumn(MenuItemData::getName)
                .setHeader("Название")
                .setSortable(true)
                .setClassNameGenerator(item -> "text-center");

        menuItemGrid.addColumn(MenuItemData::getPrice)
                .setHeader("Цена")
                .setSortable(true)
                .setClassNameGenerator(item -> "text-center");

        menuItemGrid.addColumn(data -> formatRequiredMinPrice(data))
                .setHeader("Рек. мин. цена")
                .setSortable(true)
                .setComparator(Comparator.comparingDouble(this::requiredMinPriceOrZero))
                .setClassNameGenerator(item -> "text-center");

        menuItemGrid.addColumn(data -> formatRecommendedPriceWithMargin(data))
                .setHeader("Рек. цена (с наценкой)")
                .setSortable(true)
                .setClassNameGenerator(item -> "text-center");

        var diffColumn = menuItemGrid.addComponentColumn(data -> {
                    String noData = "—";
                    Double reqMin = requiredMinPrice(data);
                    if (data.getPrice() == null || data.getPrice() == 0 || reqMin == null) {
                        Span span = new Span(noData);
                        span.getStyle().set("text-align", "center");
                        return span;
                    }
                    double diff = data.getPrice() - reqMin;
                    double pct = data.getPrice() != 0 ? diff / data.getPrice() * 100 : 0;
                    Span span = new Span(String.format("%.0f руб (%.1f%%)", diff, pct));
                    span.getStyle()
                            .set("color", diff >= 0 ? diff == 0 ? "black" : "green" : "red")
                            .set("text-align", "center");
                    span.getElement().setAttribute("title", "Факт: " + formatPrice(data.getPrice()) + ", рек. мин.: " + (reqMin != null ? formatPrice(reqMin) : noData));
                    return span;
                })
                .setComparator(Comparator.comparingDouble(data ->
                        requiredMinPriceOrZero(data) == 0 ? 0 : (data.getPrice() != null ? data.getPrice() : 0) - requiredMinPriceOrZero(data)))
                .setHeader("Разница с рек. мин.")
                .setSortable(true);

        menuItemGrid.addComponentColumn(data -> {
                    Double pct = actualFcPercent(data);
                    if (pct == null) {
                        Span s = new Span("—");
                        s.getStyle().set("text-align", "center");
                        return s;
                    }
                    Span span = new Span(String.format("%.1f%%", pct));
                    span.getStyle().set("text-align", "center");
                    Double fcCoef = data.getFcCoef();
                    if (fcCoef != null && pct > fcCoef) {
                        span.getStyle().set("color", "var(--lumo-error-color, #d32f2f)");
                    }
                    return span;
                })
                .setHeader("Доля себес. факт. %")
                .setSortable(true)
                .setComparator(Comparator.comparingDouble(d -> actualFcPercentOrZero(d)))
                .setClassNameGenerator(item -> "text-center");

        menuItemGrid.addColumn(data -> data.getFcCoef() != null ? String.format("%.2f%%", data.getFcCoef()) : "—")
                .setHeader("Себестоимость коэф")
                .setSortable(true)
                .setComparator(Comparator.comparingDouble(d -> d.getFcCoef() != null ? d.getFcCoef() : 0))
                .setClassNameGenerator(item -> "text-center");

        menuItemGrid.addColumn(data -> formatFcPriceRub(data))
                .setHeader("Себестоимость (руб)")
                .setSortable(true)
                .setComparator(Comparator.comparingDouble(d -> d.getFcPrice() != null && !Double.isNaN(d.getFcPrice()) ? d.getFcPrice() : 0))
                .setClassNameGenerator(item -> "text-center");

        menuItemGrid.addComponentColumn(data -> {
                    Span status = new Span(isBelowMinPrice(data) ? "Ниже рек. мин." : "Ок");
                    status.getStyle().set("text-align", "center");
                    if (isBelowMinPrice(data)) {
                        status.getStyle().set("color", "var(--lumo-error-color, #d32f2f)");
                    }
                    return status;
                })
                .setHeader("Статус")
                .setSortable(true)
                .setComparator(Comparator.comparingBoolean(this::isBelowMinPrice))
                .setClassNameGenerator(item -> "text-center");

        menuItemGrid.addComponentColumn(this::createActionButtons)
                .setHeader("Действия")
                .setClassNameGenerator(item -> "text-center");

        // По умолчанию сортировка по разнице с рек. мин. (сначала позиции ниже рек. мин.)
        menuItemGrid.sort(diffColumn, SortDirection.ASCENDING);
    }

    private String rowClassName(MenuItemData item) {
        return isBelowMinPrice(item) ? "below-min-price" : "";
    }

    private Double requiredMinPrice(MenuItemData data) {
        if (data.getFcPrice() == null || Double.isNaN(data.getFcPrice()) || data.getFcPrice() == 0) return null;
        if (data.getFcCoef() == null || data.getFcCoef() == 0) return null;
        return data.getFcPrice() / (data.getFcCoef() / 100);
    }

    private double requiredMinPriceOrZero(MenuItemData data) {
        Double v = requiredMinPrice(data);
        return v != null ? v : 0;
    }

    private String formatRequiredMinPrice(MenuItemData data) {
        Double v = requiredMinPrice(data);
        return v != null ? String.format("%.0f руб", v) : "—";
    }

    private String formatRecommendedPriceWithMargin(MenuItemData data) {
        Double req = requiredMinPrice(data);
        if (req == null) return "—";
        return String.format("%.0f руб", (req + 30) * 1.05);
    }

    private String formatPrice(double price) {
        return String.format("%.0f руб", price);
    }

    private Double actualFcPercent(MenuItemData data) {
        if (data.getPrice() == null || data.getPrice() == 0) return null;
        if (data.getFcPrice() == null || Double.isNaN(data.getFcPrice())) return null;
        return (data.getFcPrice() / data.getPrice()) * 100;
    }

    private double actualFcPercentOrZero(MenuItemData data) {
        Double v = actualFcPercent(data);
        return v != null ? v : 0;
    }

    private boolean isBelowMinPrice(MenuItemData data) {
        if (data.getPrice() == null || data.getPrice() <= 0) return false;
        Double req = requiredMinPrice(data);
        return req != null && data.getPrice() < req;
    }

    private String formatFcPriceRub(MenuItemData data) {
        if (data.getFcPrice() == null || Double.isNaN(data.getFcPrice())) return "—";
        return String.format("%.2f руб", data.getFcPrice());
    }

    private void updateSummary(List<MenuItemData> items) {
        summaryLayout.removeAll();
        int total = items.size();
        long belowMin = items.stream().filter(this::isBelowMinPrice).count();
        double sumPrice = 0;
        double sumFc = 0;
        int withPrice = 0;
        for (MenuItemData d : items) {
            if (d.getPrice() != null && d.getPrice() > 0) {
                sumPrice += d.getPrice();
                sumFc += (d.getFcPrice() != null && !Double.isNaN(d.getFcPrice())) ? d.getFcPrice() : 0;
                withPrice++;
            }
        }
        double avgMarginPct = (withPrice > 0 && sumPrice > 0) ? (1 - sumFc / sumPrice) * 100 : 0;

        summaryLayout.add(
                new Span("Всего позиций: " + total),
                new Span("Ниже рек. мин. цены: " + belowMin + (belowMin > 0 ? " ⚠" : "")),
                new Span("Сред. маржа: " + String.format("%.1f%%", avgMarginPct))
        );
        summaryLayout.getChildren().forEach(c -> c.getElement().getStyle().set("margin-right", "1.5em"));
        if (belowMin > 0) {
            summaryLayout.getChildren().skip(1).findFirst().ifPresent(c -> c.getElement().getStyle().set("color", "var(--lumo-error-color, #d32f2f)"));
        }
    }

    private HorizontalLayout createActionButtons(MenuItemData menuItem) {
        Button editButton = new Button("Изменить", event -> {
            if (formLayout != null) {
                remove(formLayout); // Удаляем старую форму если есть
            }
            formLayout = createForm(); // Создаем новую форму
            loadMenuItemIntoForm(menuItem);
            addComponentAtIndex(0, formLayout); // Добавляем форму в начало
        });

        editButton.getStyle().set("margin-right", "0.5em");
        return new HorizontalLayout(editButton);
    }

    private void loadMenuItemIntoForm(MenuItemData menuItem) {
        this.currentEditingMenuItem = menuItem;
        if (menuItem.getFlow() != null) {
            priceField.setValue(menuItem.getPrice());
            fcCoefField.setValue(menuItem.getFcCoef());
        } else {
            priceField.clear();
            fcCoefField.clear();
        }
        saveButton.setText("Изменить пункт меню");
        cancelButton.setVisible(true);
    }

    private FormLayout createForm() {
        priceField.setPlaceholder("Введите цену");
        fcCoefField.setPlaceholder("Введите % себестоимости");

        cancelButton.setVisible(false);
        cancelButton.addClickListener(e -> {
            remove(formLayout);
            clearForm();
        });

        saveButton.addClickListener(e -> {
            if (currentEditingMenuItem != null) {
                createOrUpdateMenuItem(currentEditingMenuItem.getId());
            }
        });
        saveButton.getStyle().set("min-width", "150px");

        FormLayout newFormLayout = new FormLayout();
        newFormLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2)
        );

        newFormLayout.add(priceField, fcCoefField, new HorizontalLayout(saveButton, cancelButton));
        return newFormLayout;
    }

    private void createOrUpdateMenuItem(Long id) {
        MenuItemData menuItemData = MenuItemData.builder()
                .id(id)
                .fcCoef(fcCoefField.getValue())
                .price(priceField.getValue())
                .build();

        menuItemService.saveMenuPrice(menuItemData);
        Notification.show("Изменения сохранены!");
        remove(formLayout); // Удаляем форму после сохранения
        clearForm();
        updateGrid(); // Перезагружаем таблицу
    }

    private void clearForm() {
        priceField.clear();
        fcCoefField.clear();
        currentEditingMenuItem = null;
        saveButton.setText("Изменить");
        cancelButton.setVisible(false);
    }

    private void updateGrid() {
        List<MenuItemData> menuItems = menuItemService.getAllMenuItemsDTO();
        menuItemGrid.setItems(menuItems);
        updateSummary(menuItems);
    }
}