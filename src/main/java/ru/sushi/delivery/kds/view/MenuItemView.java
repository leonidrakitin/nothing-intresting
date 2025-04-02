package ru.sushi.delivery.kds.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import ru.sushi.delivery.kds.domain.controller.dto.MenuItemData;
import ru.sushi.delivery.kds.domain.persist.entity.flow.Flow;
import ru.sushi.delivery.kds.domain.persist.repository.flow.FlowRepository;
import ru.sushi.delivery.kds.domain.service.MenuItemService;

import java.text.DecimalFormat;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;

@Route(value = "menu-items")
@PageTitle("Пункты меню | Доставка Суши")
@PermitAll
public class MenuItemView extends VerticalLayout {

    private final DecimalFormat priceFactFormat = new DecimalFormat("#");

    private final MenuItemService menuItemService;
    private final FlowRepository flowRepository;

    private final Grid<MenuItemData> menuItemGrid = new Grid<>();

    private final TextField nameField = new TextField("Название");
    private final ComboBox<Flow> flowComboBox = new ComboBox<>("Отображение");
    private final NumberField timeToCookSecField = new NumberField("Время приготовления (сек)");
    private final NumberField priceField = new NumberField("Цена (рублей)");

    // Кнопка, которая переключается между "Добавить" и "Изменить"
    private final Button saveButton = new Button("Добавить пункт меню");
    // Кнопка "Отменить изменения"
    private final Button cancelButton = new Button("Отменить изменения");

    // Текущий пункт меню, который редактируем (если null — значит режим добавления)
    private MenuItemData currentEditingMenuItem = null;

    @Autowired
    public MenuItemView(MenuItemService menuItemService, FlowRepository flowRepository) {
        this.menuItemService = menuItemService;
        this.flowRepository = flowRepository;

        setSizeFull();

        configureGrid();
        add(createForm(), menuItemGrid);

        updateGrid();
    }

    /**
     * Настраиваем Grid: колонки + колонка "Действия" (Изменить/Удалить).
     */
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
                        Double.isNaN(data.getFcPrice()) ? 0.0 : data.getFcPrice()/ getCoef(data)
                )))
                .setHeader("Расчет. цены")
                .setSortable(true)
                .setClassNameGenerator(item -> "text-center");

        menuItemGrid.addColumn(data -> String.format(
                        "%.0f (%.01f%%)",
                        data.getPrice() - Double.parseDouble(
                                Double.isNaN(data.getFcPrice()) ? String.valueOf(0.0) : priceFactFormat.format(data.getFcPrice()/ getCoef(data))
                        ),
                        (data.getPrice() - Double.parseDouble(
                                Double.isNaN(data.getFcPrice()) ? String.valueOf(0.0) : priceFactFormat.format(data.getFcPrice()/ getCoef(data))
                        )) / data.getPrice() * 100
                ))
                .setHeader("Изменение цены")
                .setSortable(true)
                .setClassNameGenerator(item -> "text-center");

        menuItemGrid.addColumn(data -> String.format("%.2f", data.getFcPrice()))
                .setHeader("Себестоимость")
                .setSortable(true)
                .setComparator(Comparator.comparingDouble(MenuItemData::getFcPrice))
                .setClassNameGenerator(item -> "text-center");

        menuItemGrid.addColumn(MenuItemData::getFlow)
                .setHeader("Отображение")
                .setSortable(true)
                .setClassNameGenerator(item -> "text-center");

        menuItemGrid.addColumn(item -> item.getTimeToCook().toSeconds())
                .setHeader("Время готовки (сек)")
                .setSortable(true)
                .setClassNameGenerator(item -> "text-center");

        // Добавляем колонку «Действия»
        menuItemGrid.addComponentColumn(this::createActionButtons)
                .setHeader("Действия")
                .setClassNameGenerator(item -> "text-center");
    }

    private static double getCoef(MenuItemData data) {
        return data.getName().toLowerCase().contains("фила") ? 0.4 : 0.32;
    }

    /**
     * Создаём горизонтальный лэйаут с кнопками "Изменить" и "Удалить"
     */
    private HorizontalLayout createActionButtons(MenuItemData menuItem) {
        Button editButton = new Button("Изменить", event -> {
            loadMenuItemIntoForm(menuItem);
        });

        Button deleteButton = new Button("Удалить", event -> {
            deleteMenuItem(menuItem);
        });

        editButton.getStyle().set("margin-right", "0.5em");
        return new HorizontalLayout(editButton, deleteButton);
    }

    /**
     * Загрузка выбранного пункта меню в поля формы (режим редактирования).
     */
    private void loadMenuItemIntoForm(MenuItemData menuItem) {
        this.currentEditingMenuItem = menuItem;

        // Заполняем поля
        nameField.setValue(menuItem.getName() != null ? menuItem.getName() : "");
        // Найдём Flow по названию из DTO (menuItem.getFlow()) и выставим в ComboBox
        if (menuItem.getFlow() != null) {
            Flow flow = flowRepository.findAll().stream()
                    .filter(f -> f.getName().equals(menuItem.getFlow()))
                    .findFirst()
                    .orElse(null);
            flowComboBox.setValue(flow);
            timeToCookSecField.setValue((double) menuItem.getTimeToCook().toSeconds());
            priceField.setValue(menuItem.getPrice());
        }
        else {
            flowComboBox.clear();
            timeToCookSecField.clear();
            priceField.clear();
        }

        // Меняем текст кнопки
        saveButton.setText("Изменить пункт меню");
        // Делаем кнопку «Отменить» видимой
        cancelButton.setVisible(true);
    }

    /**
     * Создаёт форму с TextField (название), ComboBox (поток) и кнопками
     */
    private FormLayout createForm() {
        nameField.setPlaceholder("Введите название пункта меню");
        timeToCookSecField.setPlaceholder("Введите количество секунд");
        priceField.setPlaceholder("Введите цену");

        // Список всех доступных потоков (Flow)
        List<Flow> flows = flowRepository.findAll();
        flowComboBox.setItems(flows);
        flowComboBox.setItemLabelGenerator(Flow::getName);
        flowComboBox.setPlaceholder("Выберите ...");

        // Кнопка «Отменить изменения» изначально скрыта
        cancelButton.setVisible(false);
        cancelButton.addClickListener(e -> clearForm());

        // Логика при нажатии на основную кнопку
        saveButton.addClickListener(e -> {
            if (currentEditingMenuItem == null) {
                // Режим добавления
                createOrUpdateMenuItem(null);
            }
            else {
                // Режим редактирования
                createOrUpdateMenuItem(currentEditingMenuItem.getId());
            }
        });
        saveButton.getStyle().set("min-width", "150px");

        // Создаём FormLayout
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),   // все компоненты в один столбец
                new FormLayout.ResponsiveStep("600px", 2) // два компонента в ряд
        );

        // Добавляем компоненты
        formLayout.add(
                nameField,
                flowComboBox,
                timeToCookSecField,
                priceField,
                new HorizontalLayout(saveButton, cancelButton)
        );

        return formLayout;
    }

    /**
     * Создаём или обновляем пункт меню (если id != null, значит обновляем).
     */
    private void createOrUpdateMenuItem(Long id) {
        String name = nameField.getValue();
        Flow selectedFlow = flowComboBox.getValue();

        if (name == null || name.isEmpty() || selectedFlow == null) {
            Notification.show("Название и отображение обязательны для заполнения!");
            return;
        }

        long expirationSec = timeToCookSecField.getValue() != null
                ? timeToCookSecField.getValue().longValue()
                : 0;
        Duration timeToCookDuration = Duration.ofSeconds(expirationSec);

        // Собираем DTO
        MenuItemData menuItemData = MenuItemData.builder()
                .id(id)
                .name(name)
                .flow(selectedFlow.getName())
                .timeToCook(timeToCookDuration)
                .price(priceField.getValue())
                .build();

        // Сохраняем (сервис сам определит create/update по наличию id)
        menuItemService.saveMenuItem(menuItemData);

        Notification.show(id == null ? "Пункт меню успешно добавлен!" : "Изменения сохранены!");
        clearForm();
        updateGrid();
    }

    /**
     * Удаляем пункт меню
     */
    private void deleteMenuItem(MenuItemData menuItem) {
        if (menuItem.getId() == null) {
            Notification.show("Не удалось удалить: отсутствует ID пункта меню.");
            return;
        }
        menuItemService.deleteMenuItem(menuItem);
        Notification.show("Пункт меню удалён!");
        updateGrid();

        // Если удалили тот, который редактировали, сбрасываем форму
        if (currentEditingMenuItem != null && currentEditingMenuItem.getId().equals(menuItem.getId())) {
            clearForm();
        }
    }

    /**
     * Очищаем форму и возвращаемся в режим добавления
     */
    private void clearForm() {
        nameField.clear();
        flowComboBox.clear();
        timeToCookSecField.clear();
        priceField.clear();
        currentEditingMenuItem = null;
        saveButton.setText("Добавить пункт меню");
        cancelButton.setVisible(false);
    }

    /**
     * Обновляет данные в Grid
     */
    private void updateGrid() {
        List<MenuItemData> menuItems = menuItemService.getAllMenuItemsDTO();
        menuItemGrid.setItems(menuItems);
    }
}
