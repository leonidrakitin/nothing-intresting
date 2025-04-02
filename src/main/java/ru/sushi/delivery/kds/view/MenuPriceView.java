package ru.sushi.delivery.kds.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.PageTitle;
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
public class MenuPriceView extends VerticalLayout {

    private final DecimalFormat priceFactFormat = new DecimalFormat("#");
    private final DecimalFormat foodCostFormat = new DecimalFormat("#.##");

    private final MenuItemService menuItemService;
    private final FlowRepository flowRepository;

    private final Grid<MenuItemData> menuItemGrid = new Grid<>();
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
        configureGrid();
        add(menuItemGrid); // Изначально добавляем только грид
        updateGrid();
    }

    private void configureGrid() {
        menuItemGrid.setSizeFull();

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

        menuItemGrid.addColumn(data -> Double.valueOf(priceFactFormat.format(
                        data.getFcPrice() == null || Double.isNaN(data.getFcPrice())
                                ? 0.0
                                : data.getFcPrice()/(data.getFcCoef()/100)
                )))
                .setHeader("Расчет. цены")
                .setSortable(true)
                .setClassNameGenerator(item -> "text-center");

        menuItemGrid.addComponentColumn(data -> {
                    if (data.getPrice() == 0 || data.getFcPrice() == null || data.getFcPrice().isNaN() || data.getFcPrice() == 0) {
                        return new Span("Нет данных");
                    }
                    double priceChange = data.getPrice() - Double.parseDouble(
                            Double.isNaN(data.getFcPrice())
                                    ? String.valueOf(0.0)
                                    : priceFactFormat.format(data.getFcPrice()/(data.getFcCoef()/100))
                    );
                    double percentage = priceChange / data.getPrice() * 100;
                    Span span = new Span(String.format("%.0f (%.01f%%)", priceChange, percentage));
                    span.getStyle()
                            .set("color", priceChange >= 0 ? priceChange == 0 ? "black" : "green" : "red")
                            .set("text-align", "center");
                    return span;
                })
                .setComparator(Comparator.comparingDouble(data ->
                        data.getFcPrice() == null || Double.isNaN(data.getFcPrice()) || data.getFcPrice() == 0
                                ? 0
                                : data.getPrice() - data.getFcPrice()/(data.getFcCoef()/100)
                ))
                .setHeader("Изменение цены")
                .setSortable(true);

        menuItemGrid.addColumn(data -> String.format("%.2f%%", data.getFcCoef()))
                .setHeader("Себестоимость коэф")
                .setSortable(true)
                .setComparator(Comparator.comparingDouble(MenuItemData::getFcCoef))
                .setClassNameGenerator(item -> "text-center");

        menuItemGrid.addColumn(data -> foodCostFormat.format(data.getFcPrice()))
                .setHeader("Себестоимость цена")
                .setSortable(true)
                .setComparator(Comparator.comparingDouble(MenuItemData::getFcPrice))
                .setClassNameGenerator(item -> "text-center");

        menuItemGrid.addComponentColumn(this::createActionButtons)
                .setHeader("Действия")
                .setClassNameGenerator(item -> "text-center");
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
    }
}