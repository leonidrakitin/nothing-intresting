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
import ru.sushi.delivery.kds.domain.controller.dto.PrepackData;
import ru.sushi.delivery.kds.domain.controller.dto.PrepackRecipeData;
import ru.sushi.delivery.kds.domain.controller.dto.SourceDto;
import ru.sushi.delivery.kds.domain.service.PrepackService;
import ru.sushi.delivery.kds.domain.service.RecipeService;
import ru.sushi.delivery.kds.domain.service.SourceItemService;

import java.util.Collections;
import java.util.List;

@Route("prepack-recipe")
@PageTitle("Список заготовок")
public class PrepackRecipeView extends VerticalLayout {

    private final PrepackService prepackService;
    private final RecipeService recipeService;
    private final SourceItemService sourceItemService;

    // Комбо-бокс для выбора заготовки
    private final ComboBox<PrepackData> prepackComboBox = new ComboBox<>("Выберите заготовку");

    // Таблица для отображения рецепта выбранной заготовки
    private final Grid<PrepackRecipeData> recipeGrid = new Grid<>(PrepackRecipeData.class, false);

    // --- Форма для добавления / редактирования строки рецепта ---
    private final ComboBox<SourceDto> sourceComboBox = new ComboBox<>("Источник");
    private final NumberField initAmountField = new NumberField("Изначальное кол-во");
    private final NumberField finalAmountField = new NumberField("Итоговое кол-во");
    private final NumberField lossesAmountField = new NumberField("Потери");
    private final NumberField lossesPercentField = new NumberField("Потери (%)");

    private final HorizontalLayout totalsLayout = new HorizontalLayout();

    // Кнопки
    private final Button saveButton = new Button("Добавить в рецепт");
    private final Button cancelButton = new Button("Отменить изменения");

    // Текущая выбранная заготовка (выпадающий список)
    private PrepackData selectedPrepack;

    // Текущая строка рецепта, которую редактируем (null, если добавляем новую)
    private PrepackRecipeData currentEditingRecipe = null;

    @Autowired
    public PrepackRecipeView(PrepackService prepackService,
                             RecipeService recipeService,
                             SourceItemService sourceItemService) {
        this.prepackService = prepackService;
        this.recipeService = recipeService;
        this.sourceItemService = sourceItemService;

        initMainComboBox();
        initRecipeGrid();
        initTotalsLayout(); // Инициализация итогов
        initRecipeForm();

        // Размещаем компоненты на странице
        add(
                prepackComboBox,
                recipeGrid,
                totalsLayout, // Убедитесь, что эта строка присутствует
                new Hr(),
                createAddFormLayout()
        );

        // Сразу после инициализации форма неактивна, пока не выбрали заготовку
        enableAddForm(false);
    }

    /**
     * Инициализация основного ComboBox (список заготовок)
     */
    private void initMainComboBox() {
        List<PrepackData> allPrepacks = prepackService.getAllPrepacks();
        prepackComboBox.setItems(allPrepacks);
        prepackComboBox.setItemLabelGenerator(PrepackData::getName);
        prepackComboBox.setPlaceholder("Поиск заготовки...");
        prepackComboBox.setClearButtonVisible(true);
        prepackComboBox.setWidth("400px");

        prepackComboBox.addValueChangeListener(event -> {
            selectedPrepack = event.getValue();
            boolean enableForm = (selectedPrepack != null);
            enableAddForm(enableForm);

            if (enableForm) {
                refreshRecipeGrid(selectedPrepack.getId());
            }
            else {
                recipeGrid.setItems(Collections.emptyList());
            }
        });
    }

    /**
     * Настраиваем таблицу (Grid) + добавляем колонку "Действия"
     */
    private void initRecipeGrid() {
        recipeGrid.addColumn(PrepackRecipeData::getSourceName)
                .setHeader("Источник");
        recipeGrid.addColumn(PrepackRecipeData::getInitAmount)
                .setHeader("Изнач. кол-во");
        recipeGrid.addColumn(PrepackRecipeData::getFinalAmount)
                .setHeader("Итог. кол-во");
        recipeGrid.addColumn(PrepackRecipeData::getLossesAmount)
                .setHeader("Потери");
        recipeGrid.addColumn(PrepackRecipeData::getLossesPercentage)
                .setHeader("Потери (%)");
        recipeGrid.addColumn(PrepackRecipeData::getFcCost)
                .setHeader("Себестоимость");

        // Добавляем колонку "Действия" с кнопками "Изменить"/"Удалить"
        recipeGrid.addComponentColumn(this::createActionButtons)
                .setHeader("Действия")
                .setAutoWidth(true);
    }

    /**
     * Создаём горизонтальный лэйаут с кнопками "Изменить" и "Удалить"
     */
    private HorizontalLayout createActionButtons(PrepackRecipeData recipeItem) {
        Button editButton = new Button("Изменить", e -> loadRecipeItemIntoForm(recipeItem));
        Button deleteButton = new Button("Удалить", e -> deleteRecipeItem(recipeItem));

        HorizontalLayout layout = new HorizontalLayout(editButton, deleteButton);
        layout.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        return layout;
    }

    /**
     * Загрузить выбранную строку рецепта в форму (режим редактирования)
     */
    private void loadRecipeItemIntoForm(PrepackRecipeData recipeItem) {
        this.currentEditingRecipe = recipeItem;

        // Заполняем поля
        // Предполагается, что у PrepackRecipeData есть sourceId, sourceName и т.д.
        // Выбираем sourceComboBox по ID, если нужно
        if (recipeItem.getSourceName() != null) {
            // Ищем SourceDto по имени (или по id, если есть)
            // Ниже - поиск по имени (не всегда идеален):
            SourceDto sourceDto = sourceItemService.getAllSources().stream()
                    .filter(s -> s.getName().equals(recipeItem.getSourceName()))
                    .findFirst()
                    .orElse(null);
            sourceComboBox.setValue(sourceDto);
        }
        else {
            sourceComboBox.clear();
        }

        initAmountField.setValue(recipeItem.getInitAmount() != null ? recipeItem.getInitAmount() : 0.0);
        finalAmountField.setValue(recipeItem.getFinalAmount() != null ? recipeItem.getFinalAmount() : 0.0);
        lossesAmountField.setValue(recipeItem.getLossesAmount() != null ? recipeItem.getLossesAmount() : 0.0);
        lossesPercentField.setValue(recipeItem.getLossesPercentage() != null ? recipeItem.getLossesPercentage() : 0.0);

        saveButton.setText("Изменить в рецепте");
        cancelButton.setVisible(true);
    }

    /**
     * Инициализация формы (ComboBox источника, поля, кнопки)
     */
    private void initRecipeForm() {
        List<SourceDto> allSources = sourceItemService.getAllSources();
        sourceComboBox.setItems(allSources);
        sourceComboBox.setItemLabelGenerator(sourceDto -> sourceDto.getName() + " " + sourceDto.getType());
        sourceComboBox.setPlaceholder("Ингредиент или заготовка...");
        sourceComboBox.setClearButtonVisible(true);

        initAmountField.setValue(0.0);
        finalAmountField.setValue(0.0);
        lossesAmountField.setValue(0.0);
        lossesPercentField.setValue(0.0);

        initAmountField.setMin(0);
        finalAmountField.setMin(0);
        lossesAmountField.setMin(0);
        lossesPercentField.setMin(0);

        lossesAmountField.setReadOnly(true);
        lossesPercentField.setReadOnly(true);

        initAmountField.addValueChangeListener(e -> recalcLosses());
        finalAmountField.addValueChangeListener(e -> recalcLosses());

        // По умолчанию «Отменить изменения» скрыта
        cancelButton.setVisible(false);
        cancelButton.addClickListener(e -> clearForm());

        // Переключение логики сохранения (добавление/редактирование)
        saveButton.addClickListener(event -> {
            if (selectedPrepack == null) {
                Notification.show("Сначала выберите заготовку");
                return;
            }
            SourceDto chosenSource = sourceComboBox.getValue();
            if (chosenSource == null) {
                Notification.show("Выберите источник");
                return;
            }
            if (currentEditingRecipe == null) {
                // Режим добавления
                createOrUpdateRecipe(null, chosenSource);
            }
            else {
                // Режим редактирования
                createOrUpdateRecipe(currentEditingRecipe.getId(), chosenSource);
            }
        });
    }

    /**
     * Создаём лэйаут, в который кладём поля и кнопки
     */
    private VerticalLayout createAddFormLayout() {
        HorizontalLayout fieldsLayout = new HorizontalLayout(
                sourceComboBox,
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
     * Создаёт новую или обновляет существующую строку рецепта (если id != null)
     */
    private void createOrUpdateRecipe(Long recipeId, SourceDto chosenSource) {
        PrepackRecipeData recipeDto = PrepackRecipeData.builder()
                .id(recipeId) // null => создаём новую, не null => обновляем
                .initAmount(initAmountField.getValue())
                .finalAmount(finalAmountField.getValue())
                .lossesAmount(lossesAmountField.getValue())
                .lossesPercentage(lossesPercentField.getValue())
                .build();

        // Сохраняем (create/update)
        recipeService.savePrepackRecipe(recipeDto, chosenSource, selectedPrepack.getId());

        Notification.show(recipeId == null
                ? "Успешно добавлено в рецепт!"
                : "Изменения в рецепте сохранены!");

        clearForm();
        refreshRecipeGrid(selectedPrepack.getId());
    }

    /**
     * Удаляем строку рецепта
     */
    private void deleteRecipeItem(PrepackRecipeData recipeItem) {
        if (recipeItem.getId() == null) {
            Notification.show("Невозможно удалить: отсутствует ID рецепта.");
            return;
        }
        recipeService.deletePrepackRecipe(recipeItem);

        Notification.show("Строка рецепта удалена!");
        refreshRecipeGrid(selectedPrepack.getId());

        // Если удалили то, что редактировали, сбросим форму
        if (currentEditingRecipe != null
                && currentEditingRecipe.getId() != null
                && currentEditingRecipe.getId().equals(recipeItem.getId())) {
            clearForm();
        }
    }

    /**
     * Пересчёт полей "Потери" и "Потери (%)"
     */
    private void recalcLosses() {
        double init = initAmountField.getValue() != null ? initAmountField.getValue() : 0.0;
        double fin = finalAmountField.getValue() != null ? finalAmountField.getValue() : 0.0;

        double losses = init - fin;
        if (losses < 0) {
            losses = 0.0;
        }

        double percentage = 0.0;
        if (init != 0) {
            percentage = (losses / init) * 100.0;
            percentage = Math.round(percentage * 100.0) / 100.0;
        }

        lossesAmountField.setValue(losses);
        lossesPercentField.setValue(percentage);
    }

    /**
     * Очищаем поля формы и возвращаемся в режим добавления
     */
    private void clearForm() {
        sourceComboBox.clear();
        initAmountField.setValue(0.0);
        finalAmountField.setValue(0.0);
        lossesAmountField.setValue(0.0);
        lossesPercentField.setValue(0.0);

        currentEditingRecipe = null;
        saveButton.setText("Добавить в рецепт");
        cancelButton.setVisible(false);
    }

    /**
     * Включаем или выключаем форму добавления (зависит от выбранной заготовки)
     */
    private void enableAddForm(boolean enable) {
        sourceComboBox.setEnabled(enable);
        initAmountField.setEnabled(enable);
        finalAmountField.setEnabled(enable);
        lossesAmountField.setEnabled(enable);
        lossesPercentField.setEnabled(enable);
        saveButton.setEnabled(enable);
        cancelButton.setEnabled(enable);
    }

    /**
     * Обновляем таблицу рецепта для выбранной заготовки
     */
    private void refreshRecipeGrid(Long prepackId) {
        List<PrepackRecipeData> recipeList =
                recipeService.getPrepackRecipeByPrepackId(prepackId);
        recipeGrid.setItems(recipeList);
        updateTotals(); // Обновляем итоги после изменения данных
    }

    private void initTotalsLayout() {
        totalsLayout.setWidthFull();
        totalsLayout.setJustifyContentMode(JustifyContentMode.START);
        updateTotals(); // Изначально пустые значения
    }

    private void updateTotals() {
        List<PrepackRecipeData> items = recipeGrid.getListDataView().getItems().toList();

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
        double totalFcCostFor1Kg = totalFcCost * 1000 / totalFinalAmount;

        // Очищаем предыдущие значения
        totalsLayout.removeAll();

        // Добавляем новые итоговые значения
        totalsLayout.add(
                new Span("Изнач. кол-во: " + String.format("%.2fг", totalInitAmount)),
                new Span("Итог. кол-во: " + String.format("%.2fг", totalFinalAmount)),
                new Span("Потери: " + String.format("%.2f руб", totalLossesAmount)),
                new Span("Себестоимость: " + String.format("%.2f руб", totalFcCost)),
                new Span("Себестоимость за 1кг: " + String.format("%.2f рубц", totalFcCostFor1Kg))
        );

        // Устанавливаем отступы между элементами
        totalsLayout.getChildren().forEach(component ->
                component.getElement().getStyle().set("margin-right", "20px"));
    }
}
