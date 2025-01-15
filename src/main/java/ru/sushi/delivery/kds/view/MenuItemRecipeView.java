package ru.sushi.delivery.kds.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import ru.sushi.delivery.kds.domain.controller.dto.MenuItemDto;
import ru.sushi.delivery.kds.domain.controller.dto.MenuItemRecipeDto;
import ru.sushi.delivery.kds.domain.controller.dto.SourceDto;
import ru.sushi.delivery.kds.domain.persist.entity.flow.Station;
import ru.sushi.delivery.kds.domain.service.MenuItemService;
import ru.sushi.delivery.kds.domain.service.RecipeService;
import ru.sushi.delivery.kds.domain.service.SourceService;
import ru.sushi.delivery.kds.domain.service.StationService;

import java.util.Collections;
import java.util.List;

@Route("menu-item-recipe")
@PageTitle("Рецепты для блюд")
public class MenuItemRecipeView extends VerticalLayout {

    private final MenuItemService menuItemService;
    private final RecipeService recipeService;
    private final SourceService sourceService;
    private final StationService stationService;

    // Комбо-бокс для выбора блюда (MenuItem)
    private final ComboBox<MenuItemDto> menuItemComboBox = new ComboBox<>("Выберите блюдо");

    // Таблица для отображения рецепта выбранного блюда
    private final Grid<MenuItemRecipeDto> recipeGrid = new Grid<>(MenuItemRecipeDto.class, false);

    // --- Форма для добавления нового ингредиента/заготовки в рецепт ---
    // Комбо-бокс для выбора источника (ингредиент / заготовка)
    private final ComboBox<SourceDto> sourceComboBox = new ComboBox<>("Источник");

    // Комбо-бокс для выбора станции
    private final ComboBox<Station> stationComboBox = new ComboBox<>("Станция");

    // Поля для ввода значений
    private final NumberField initAmountField = new NumberField("Изначальное кол-во");
    private final NumberField finalAmountField = new NumberField("Итоговое кол-во");
    private final NumberField lossesAmountField = new NumberField("Потери");
    private final NumberField lossesPercentField = new NumberField("Потери (%)");

    private final Button addButton = new Button("Добавить в рецепт");

    private MenuItemDto selectedMenuItem; // Текущее выбранное блюдо

    @Autowired
    public MenuItemRecipeView(MenuItemService menuItemService,
                              RecipeService recipeService,
                              SourceService sourceService,
                              StationService stationService) {
        this.menuItemService = menuItemService;
        this.recipeService = recipeService;
        this.sourceService = sourceService;
        this.stationService = stationService;

        initMainComboBox();
        initRecipeGrid();
        initAddRecipeForm();

        add(
                menuItemComboBox,
                recipeGrid,
                new Hr(),
                createAddFormLayout()
        );

        // Форма добавления ингредиентов недоступна, пока не выбрано блюдо
        enableAddForm(false);
    }

    /**
     * Инициализация ComboBox для выбора блюда из меню
     */
    private void initMainComboBox() {
        // Загружаем все блюда
        List<MenuItemDto> allMenuItems = menuItemService.getAllMenuItemsDTO();
        menuItemComboBox.setItems(allMenuItems);

        // Отображать только name
        menuItemComboBox.setItemLabelGenerator(MenuItemDto::getName);

        // Настройки визуала
        menuItemComboBox.setPlaceholder("Поиск блюда...");
        menuItemComboBox.setClearButtonVisible(true);
        menuItemComboBox.setWidth("400px");

        // Слушатель выбора блюда
        menuItemComboBox.addValueChangeListener(event -> {
            selectedMenuItem = event.getValue();
            boolean enableForm = (selectedMenuItem != null);
            enableAddForm(enableForm);

            if (enableForm) {
                refreshRecipeGrid(selectedMenuItem.getId());
            }
            else {
                recipeGrid.setItems(Collections.emptyList());
            }
        });
    }

    /**
     * Инициализация таблицы для отображения рецепта выбранного блюда
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
    }

    /**
     * Инициализация формы для добавления новой строки в рецепт
     */
    private void initAddRecipeForm() {
        // Список всех возможных источников
        List<SourceDto> allSources = sourceService.getAllSources();
        sourceComboBox.setItems(allSources);
        sourceComboBox.setItemLabelGenerator(SourceDto::getName);
        sourceComboBox.setPlaceholder("Ингредиент или заготовка...");
        sourceComboBox.setClearButtonVisible(true);

        // Список всех станций
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

        // Поля потерь доступны только для чтения
        lossesAmountField.setReadOnly(true);
        lossesPercentField.setReadOnly(true);

        // Обработчики изменения для пересчёта потерь
        initAmountField.addValueChangeListener(e -> recalcLosses());
        finalAmountField.addValueChangeListener(e -> recalcLosses());

        // Нажатие на кнопку "Добавить"
        addButton.addClickListener(e -> {
            if (selectedMenuItem == null) {
                Notification.show("Сначала выберите блюдо");
                return;
            }
            SourceDto chosenSource = sourceComboBox.getValue();
            if (chosenSource == null) {
                Notification.show("Выберите источник");
                return;
            }
            Station chosenStation = stationComboBox.getValue();
            if (chosenStation == null) {
                Notification.show("Выберите станцию");
                return;
            }

            // Создаём DTO для сохранения
            MenuItemRecipeDto recipeDto = MenuItemRecipeDto.builder()
                    .stationId(chosenStation.getId())
                    .initAmount(initAmountField.getValue())
                    .finalAmount(finalAmountField.getValue())
                    .lossesAmount(lossesAmountField.getValue())
                    .lossesPercentage(lossesPercentField.getValue())
                    .build();

            // Сохраняем в БД
            recipeService.saveMenuRecipe(recipeDto, chosenSource, selectedMenuItem.getId());

            Notification.show("Успешно добавлено!");

            // Очищаем форму и обновляем таблицу
            clearAddForm();
            refreshRecipeGrid(selectedMenuItem.getId());
        });
    }

    /**
     * Создаём макет формы добавления ингредиента/заготовки к блюду
     */
    private VerticalLayout createAddFormLayout() {
        HorizontalLayout fieldsLayout = new HorizontalLayout(
                sourceComboBox,
                stationComboBox,
                initAmountField,
                finalAmountField,
                lossesAmountField,
                lossesPercentField,
                addButton
        );
        fieldsLayout.setDefaultVerticalComponentAlignment(Alignment.END);

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.add(fieldsLayout);
        return layout;
    }

    /**
     * Включить/выключить доступность формы добавления
     */
    private void enableAddForm(boolean enable) {
        sourceComboBox.setEnabled(enable);
        stationComboBox.setEnabled(enable);
        initAmountField.setEnabled(enable);
        finalAmountField.setEnabled(enable);
        lossesAmountField.setEnabled(enable);
        lossesPercentField.setEnabled(enable);
        addButton.setEnabled(enable);
    }

    /**
     * Пересчитать потери/процент потерь при изменении initAmount или finalAmount
     */
    private void recalcLosses() {
        double init = initAmountField.getValue() != null ? initAmountField.getValue() : 0.0;
        double fin = finalAmountField.getValue() != null ? finalAmountField.getValue() : 0.0;

        double losses = init - fin;
        if (losses < 0) {
            losses = 0.0; // Если вдруг итоговое кол-во больше изначального
        }

        double percentage = 0.0;
        if (init > 0) {
            percentage = (losses / init) * 100.0;
            // округляем до сотых
            percentage = Math.round(percentage * 100.0) / 100.0;
        }

        lossesAmountField.setValue(losses);
        lossesPercentField.setValue(percentage);
    }

    /**
     * Очистить поля формы
     */
    private void clearAddForm() {
        sourceComboBox.clear();
        stationComboBox.clear();
        initAmountField.setValue(0.0);
        finalAmountField.setValue(0.0);
        lossesAmountField.setValue(0.0);
        lossesPercentField.setValue(0.0);
    }

    /**
     * Обновить таблицу рецепта для выбранного блюда
     */
    private void refreshRecipeGrid(Long menuId) {
        List<MenuItemRecipeDto> recipes = recipeService.getMenuRecipeByMenuId(menuId);
        recipeGrid.setItems(recipes);
    }
}
