package ru.sushi.delivery.kds.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import ru.sushi.delivery.kds.domain.controller.dto.MenuItemData;
import ru.sushi.delivery.kds.domain.controller.dto.MenuItemRecipeDto;
import ru.sushi.delivery.kds.domain.controller.dto.SourceDto;
import ru.sushi.delivery.kds.domain.persist.entity.flow.Station;
import ru.sushi.delivery.kds.domain.service.MenuItemService;
import ru.sushi.delivery.kds.domain.service.RecipeService;
import ru.sushi.delivery.kds.domain.service.SourceItemService;
import ru.sushi.delivery.kds.domain.service.StationService;

import java.util.Collections;
import java.util.List;

@Route("menu-item-recipe")
@PageTitle("Рецепты для блюд")
public class MenuItemRecipeView extends VerticalLayout {

    private final MenuItemService menuItemService;
    private final RecipeService recipeService;
    private final SourceItemService sourceItemService;
    private final StationService stationService;

    // ComboBox для выбора блюда (MenuItem)
    private final ComboBox<MenuItemData> menuItemComboBox = new ComboBox<>("Выберите блюдо");

    // Таблица для отображения рецепта выбранного блюда
    private final Grid<MenuItemRecipeDto> recipeGrid = new Grid<>(MenuItemRecipeDto.class, false);

    // Поля формы добавления / редактирования
    private final ComboBox<SourceDto> sourceComboBox = new ComboBox<>("Источник");
    private final ComboBox<Station> stationComboBox = new ComboBox<>("Станция");
    private final NumberField initAmountField = new NumberField("Изначальное кол-во");
    private final NumberField finalAmountField = new NumberField("Итоговое кол-во");
    private final NumberField lossesAmountField = new NumberField("Потери");
    private final NumberField lossesPercentField = new NumberField("Потери (%)");

    private final HorizontalLayout totalsLayout = new HorizontalLayout();

    // Кнопки формы
    private final Button saveButton = new Button("Добавить в рецепт");
    private final Button cancelButton = new Button("Отменить изменения");

    // Текущее выбранное блюдо
    private MenuItemData selectedMenuItem;
    // Текущая запись рецепта (для редактирования), null => режим добавления
    private MenuItemRecipeDto currentEditingRecipe = null;

    @Autowired
    public MenuItemRecipeView(MenuItemService menuItemService,
                              RecipeService recipeService,
                              SourceItemService sourceItemService,
                              StationService stationService) {
        this.menuItemService = menuItemService;
        this.recipeService = recipeService;
        this.sourceItemService = sourceItemService;
        this.stationService = stationService;

        initMainComboBox();
        initRecipeGrid();
        initTotalsLayout(); // Инициализация итогов
        initRecipeForm();

        // Размещаем на странице
        add(
                menuItemComboBox,
                recipeGrid,
                totalsLayout, // Убедитесь, что эта строка присутствует
                new Hr(),
                createRecipeFormLayout()
        );

        // Форма недоступна, пока не выбрано блюдо
        enableForm(false);
    }

    /**
     * Инициализация ComboBox для выбора блюда.
     */
    private void initMainComboBox() {
        // Список всех блюд
        List<MenuItemData> allMenuItems = menuItemService.getAllMenuItemsDTO();
        menuItemComboBox.setItems(allMenuItems);

        // Только название
        menuItemComboBox.setItemLabelGenerator(MenuItemData::getName);

        // Placeholder, очистка
        menuItemComboBox.setPlaceholder("Поиск блюда...");
        menuItemComboBox.setClearButtonVisible(true);
        menuItemComboBox.setWidth("400px");

        // Обработка выбора
        menuItemComboBox.addValueChangeListener(event -> {
            selectedMenuItem = event.getValue();
            boolean enableForm = (selectedMenuItem != null);
            enableForm(enableForm);

            if (enableForm) {
                refreshRecipeGrid(selectedMenuItem.getId());
            }
            else {
                recipeGrid.setItems(Collections.emptyList());
            }
        });
    }

    /**
     * Инициализация таблицы с добавлением колонки «Действия».
     */
    private void initRecipeGrid() {
        recipeGrid.addColumn(MenuItemRecipeDto::getSourceName)
                .setHeader("Источник");
        recipeGrid.addColumn(MenuItemRecipeDto::getStationId)
                .setHeader("Станция");
        recipeGrid.addColumn(MenuItemRecipeDto::getInitAmount)
                .setHeader("Изнач. кол-во");
        recipeGrid.addColumn(MenuItemRecipeDto::getFinalAmount)
                .setHeader("Итог. кол-во");
        recipeGrid.addColumn(MenuItemRecipeDto::getLossesAmount)
                .setHeader("Потери");
        recipeGrid.addColumn(MenuItemRecipeDto::getLossesPercentage)
                .setHeader("Потери (%)");
        recipeGrid.addColumn(MenuItemRecipeDto::getFcCost)
                .setHeader("Себестоимость");

        // Колонка "Действия" — «Изменить» и «Удалить»
        recipeGrid.addComponentColumn(this::createActionButtons)
                .setHeader("Действия")
                .setAutoWidth(true);
    }

    /**
     * Создаём горизонтальный лэйаут кнопок «Изменить» и «Удалить».
     */
    private HorizontalLayout createActionButtons(MenuItemRecipeDto recipeItem) {
        Button editButton = new Button("Изменить", e -> loadRecipeIntoForm(recipeItem));
        Button deleteButton = new Button("Удалить", e -> deleteRecipeItem(recipeItem));

        HorizontalLayout layout = new HorizontalLayout(editButton, deleteButton);
        layout.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        return layout;
    }

    /**
     * Загружаем существующую запись рецепта в форму (режим редактирования).
     */
    private void loadRecipeIntoForm(MenuItemRecipeDto recipeItem) {
        this.currentEditingRecipe = recipeItem;

        // Найдём источник SourceDto (по имени или по id, если у DTO есть sourceId)
        if (recipeItem.getSourceName() != null) {
            SourceDto sourceDto = sourceItemService.getAllSources().stream()
                    .filter(s -> recipeItem.getSourceName().equals(s.getName()))
                    .findFirst()
                    .orElse(null);
            sourceComboBox.setValue(sourceDto);
        }
        else {
            sourceComboBox.clear();
        }

        // Найдём станцию (если stationId != null)
        if (recipeItem.getStationId() != null) {
            Station station = stationService.getById(recipeItem.getStationId());
            stationComboBox.setValue(station);
        }
        else {
            stationComboBox.clear();
        }

        initAmountField.setValue(recipeItem.getInitAmount() != null ? recipeItem.getInitAmount() : 0.0);
        finalAmountField.setValue(recipeItem.getFinalAmount() != null ? recipeItem.getFinalAmount() : 0.0);
        lossesAmountField.setValue(recipeItem.getLossesAmount() != null ? recipeItem.getLossesAmount() : 0.0);
        lossesPercentField.setValue(recipeItem.getLossesPercentage() != null ? recipeItem.getLossesPercentage() : 0.0);

        saveButton.setText("Изменить в рецепте");
        cancelButton.setVisible(true);
    }

    /**
     * Инициализация формы (ComboBox источника, станции, поля, кнопки).
     */
    private void initRecipeForm() {
        // Источники
        List<SourceDto> allSources = sourceItemService.getAllSources();
        sourceComboBox.setItems(allSources);
        sourceComboBox.setItemLabelGenerator(sourceDto -> String.format("%s [%s]", sourceDto.getName(), sourceDto.getType()));
        sourceComboBox.setPlaceholder("Ингредиент / заготовка...");
        sourceComboBox.setClearButtonVisible(true);

        // Станции
        List<Station> stations = stationService.getAll().stream().toList();
        stationComboBox.setItems(stations);
        stationComboBox.setItemLabelGenerator(Station::getName);
        stationComboBox.setPlaceholder("Выберите станцию...");
        stationComboBox.setClearButtonVisible(true);

        // Поля по умолчанию
        initAmountField.setValue(0.0);
        finalAmountField.setValue(0.0);
        lossesAmountField.setValue(0.0);
        lossesPercentField.setValue(0.0);

        // Минимум = 0
        initAmountField.setMin(0);
        finalAmountField.setMin(0);
        lossesAmountField.setMin(0);
        lossesPercentField.setMin(0);

        // Поля потерь - readOnly
        lossesAmountField.setReadOnly(true);
        lossesPercentField.setReadOnly(true);

        // Пересчитываем потери при изменении
        initAmountField.addValueChangeListener(e -> recalcLosses());
        finalAmountField.addValueChangeListener(e -> recalcLosses());

        // Кнопка «Отменить» изначально невидима
        cancelButton.setVisible(false);
        cancelButton.addClickListener(e -> clearForm());

        // Логика при нажатии на «Добавить / Изменить»
        saveButton.addClickListener(e -> {
            if (selectedMenuItem == null) {
                Notification.show("Сначала выберите блюдо!");
                return;
            }
            SourceDto chosenSource = sourceComboBox.getValue();
            if (chosenSource == null) {
                Notification.show("Выберите источник!");
                return;
            }
            Station chosenStation = stationComboBox.getValue();
            if (chosenStation == null) {
                Notification.show("Выберите станцию!");
                return;
            }

            if (currentEditingRecipe == null) {
                // Добавление
                createOrUpdateRecipe(null, chosenSource, chosenStation);
            }
            else {
                // Редактирование
                createOrUpdateRecipe(currentEditingRecipe.getId(), chosenSource, chosenStation);
            }
        });
        saveButton.getStyle().set("min-width", "150px");
    }

    /**
     * Создаём лейаут (layout) из полей и кнопок
     */
    private VerticalLayout createRecipeFormLayout() {
        HorizontalLayout fieldsLayout = new HorizontalLayout(
                sourceComboBox,
                stationComboBox,
                initAmountField,
                finalAmountField,
                lossesAmountField,
                lossesPercentField,
                saveButton,
                cancelButton
        );
        fieldsLayout.setDefaultVerticalComponentAlignment(Alignment.END);

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.add(fieldsLayout);
        return layout;
    }

    /**
     * Добавить или обновить запись в рецепте (если id != null — обновляем).
     */
    private void createOrUpdateRecipe(Long id, SourceDto chosenSource, Station chosenStation) {
        // Собираем DTO
        MenuItemRecipeDto recipeDto = MenuItemRecipeDto.builder()
                .id(id)  // null => новая запись, иначе обновление
                .stationId(chosenStation.getId())
                .initAmount(initAmountField.getValue())
                .finalAmount(finalAmountField.getValue())
                .lossesAmount(lossesAmountField.getValue())
                .lossesPercentage(lossesPercentField.getValue())
                .build();

        // Вызываем сервис (создание/обновление)
        recipeService.saveMenuRecipe(recipeDto, chosenSource, selectedMenuItem.getId());

        Notification.show(id == null
                ? "Успешно добавлено в рецепт!"
                : "Изменения сохранены!");

        clearForm();
        refreshRecipeGrid(selectedMenuItem.getId());
    }

    /**
     * Удалить запись рецепта
     */
    private void deleteRecipeItem(MenuItemRecipeDto recipeItem) {
        if (recipeItem.getId() == null) {
            Notification.show("Невозможно удалить, отсутствует ID рецепта.");
            return;
        }
        // Предположим, что в вашем сервисе есть метод removeMenuRecipeById или deleteMenuRecipe(...)
        recipeService.deleteMenuItemRecipe(recipeItem);
        Notification.show("Строка рецепта удалена!");

        refreshRecipeGrid(selectedMenuItem.getId());

        // Если удалили ту, что редактировалась, сбрасываем форму
        if (currentEditingRecipe != null
                && currentEditingRecipe.getId() != null
                && currentEditingRecipe.getId().equals(recipeItem.getId())) {
            clearForm();
        }
    }

    /**
     * Пересчитываем потери при изменении полей
     */
    private void recalcLosses() {
        double init = initAmountField.getValue() != null ? initAmountField.getValue() : 0.0;
        double fin = finalAmountField.getValue() != null ? finalAmountField.getValue() : 0.0;

        double losses = init - fin;
        if (losses < 0) {
            losses = 0.0;
        }

        double percentage = 0.0;
        if (init > 0) {
            percentage = (losses / init) * 100.0;
            percentage = Math.round(percentage * 100.0) / 100.0;
        }

        lossesAmountField.setValue(losses);
        lossesPercentField.setValue(percentage);
    }

    /**
     * Очищаем форму и возвращаемся в режим добавления (currentEditingRecipe = null).
     */
    private void clearForm() {
        sourceComboBox.clear();
        stationComboBox.clear();
        initAmountField.setValue(0.0);
        finalAmountField.setValue(0.0);
        lossesAmountField.setValue(0.0);
        lossesPercentField.setValue(0.0);

        currentEditingRecipe = null;
        saveButton.setText("Добавить в рецепт");
        cancelButton.setVisible(false);
    }

    /**
     * Включить или выключить форму (зависит от выбранного блюда).
     */
    private void enableForm(boolean enable) {
        sourceComboBox.setEnabled(enable);
        stationComboBox.setEnabled(enable);
        initAmountField.setEnabled(enable);
        finalAmountField.setEnabled(enable);
        lossesAmountField.setEnabled(enable);
        lossesPercentField.setEnabled(enable);
        saveButton.setEnabled(enable);
        cancelButton.setEnabled(enable);
    }

    /**
     * Обновляем таблицу для выбранного блюда.
     */
    private void refreshRecipeGrid(Long menuId) {
        List<MenuItemRecipeDto> recipes = recipeService.getMenuRecipeByMenuId(menuId);
        recipeGrid.setItems(recipes);
        updateTotals(); // Обновляем итоги после изменения данных
    }

    private void initTotalsLayout() {
        totalsLayout.setWidthFull();
        totalsLayout.setJustifyContentMode(JustifyContentMode.START);
        updateTotals(); // Изначально пустые значения
    }

    private void updateTotals() {
        List<MenuItemRecipeDto> items = recipeGrid.getListDataView().getItems().toList();

        double totalInitAmount = items.stream()
                .mapToDouble(item -> item.getInitAmount() != null ? item.getInitAmount() : 0.0)
                .sum();
        double totalFinalAmount = items.stream()
                .mapToDouble(item -> item.getFinalAmount() != null ? item.getFinalAmount() : 0.0)
                .sum();
        double totalLossesAmount = items.stream()
                .mapToDouble(item -> item.getLossesAmount() != null ? item.getLossesAmount() : 0.0)
                .sum();
        double totalFcCost = items.stream()
                .mapToDouble(item -> item.getFcCost() != null ? item.getFcCost() : 0.0)
                .sum();

        // Очищаем предыдущие значения
        totalsLayout.removeAll();

        // Добавляем новые итоговые значения
        totalsLayout.add(
                new Span("Изнач. кол-во: " + String.format("%.2f", totalInitAmount)),
                new Span("Итог. кол-во: " + String.format("%.2f", totalFinalAmount)),
                new Span("Потери: " + String.format("%.2f", totalLossesAmount)),
                new Span("Себестоимость: " + String.format("%.2f", totalFcCost))
        );

        // Устанавливаем отступы между элементами
        totalsLayout.getChildren().forEach(component ->
                component.getElement().getStyle().set("margin-right", "20px"));
    }
}
