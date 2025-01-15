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
import ru.sushi.delivery.kds.domain.controller.dto.PrepackDto;
import ru.sushi.delivery.kds.domain.controller.dto.PrepackRecipeDto;
import ru.sushi.delivery.kds.domain.controller.dto.SourceDto;
import ru.sushi.delivery.kds.domain.service.PrepackService;
import ru.sushi.delivery.kds.domain.service.RecipeService;
import ru.sushi.delivery.kds.domain.service.SourceService;

import java.util.Collections;
import java.util.List;

@Route("prepack-recipe")
@PageTitle("Список заготовок")
public class PrepackRecipeView extends VerticalLayout {

    private final PrepackService prepackService;
    private final RecipeService recipeService;
    private final SourceService sourceService;

    // Комбо-бокс для выбора заготовки
    private final ComboBox<PrepackDto> prepackComboBox = new ComboBox<>("Выберите заготовку");

    // Таблица для отображения рецепта выбранной заготовки
    private final Grid<PrepackRecipeDto> recipeGrid = new Grid<>(PrepackRecipeDto.class, false);

    // --- Форма для добавления нового ингредиента (или заготовки) в рецепт ---
    // Комбо-бокс для выбора источника (ингредиент / заготовка)
    private final ComboBox<SourceDto> sourceComboBox = new ComboBox<>("Источник");

    // Поля для ввода значений
    private final NumberField initAmountField = new NumberField("Изначальное кол-во");
    private final NumberField finalAmountField = new NumberField("Итоговое кол-во");
    private final NumberField lossesAmountField = new NumberField("Потери");
    private final NumberField lossesPercentField = new NumberField("Потери (%)");

    // Кнопка «Добавить» выносим в поле класса, чтобы управлять доступностью
    private final Button addButton = new Button("Добавить в рецепт");

    private PrepackDto selectedPrepack; // Текущая выбранная заготовка

    @Autowired
    public PrepackRecipeView(PrepackService prepackService,
                             RecipeService recipeService,
                             SourceService sourceService) {
        this.prepackService = prepackService;
        this.recipeService = recipeService;
        this.sourceService = sourceService;

        initMainComboBox();
        initRecipeGrid();
        initAddIngredientForm();

        // Раскладываем на странице
        add(
                prepackComboBox,
                recipeGrid,
                new Hr(),
                createAddFormLayout() // Форма добавления
        );

        // Сразу после инициализации делаем форму добавления неактивной,
        // пока не будет выбрана заготовка
        enableAddForm(false);
    }

    private void initMainComboBox() {
        // Загружаем все заготовки
        List<PrepackDto> allPrepacks = prepackService.getAllPrepacks();
        prepackComboBox.setItems(allPrepacks);

        // Отображать только name
        prepackComboBox.setItemLabelGenerator(PrepackDto::getName);

        // Placeholder (поиск), кнопка очистки
        prepackComboBox.setPlaceholder("Поиск заготовки...");
        prepackComboBox.setClearButtonVisible(true);

        // Сделаем шире поле поиска
        prepackComboBox.setWidth("400px");

        // Обработка выбора
        prepackComboBox.addValueChangeListener(event -> {
            selectedPrepack = event.getValue();

            // Если выбрана заготовка — включаем форму добавления, иначе выключаем
            boolean enableForm = (selectedPrepack != null);
            enableAddForm(enableForm);

            if (enableForm) {
                // Обновляем таблицу рецепта
                refreshRecipeGrid(selectedPrepack.getId());
            }
            else {
                recipeGrid.setItems(Collections.emptyList());
            }
        });
    }

    private void initRecipeGrid() {
        recipeGrid.addColumn(PrepackRecipeDto::getSourceName)
                .setHeader("Источник");

        recipeGrid.addColumn(PrepackRecipeDto::getInitAmount)
                .setHeader("Изнач. кол-во");

        recipeGrid.addColumn(PrepackRecipeDto::getFinalAmount)
                .setHeader("Итог. кол-во");

        recipeGrid.addColumn(PrepackRecipeDto::getLossesAmount)
                .setHeader("Потери");

        recipeGrid.addColumn(PrepackRecipeDto::getLossesPercentage)
                .setHeader("Потери (%)");
    }

    /**
     * Инициализируем форму для добавления ингредиента / заготовки в рецепт
     */
    private void initAddIngredientForm() {
        // Список всех источников (INGREDIENT / PREPACK)
        List<SourceDto> allSources = sourceService.getAllSources();
        sourceComboBox.setItems(allSources);
        sourceComboBox.setItemLabelGenerator(SourceDto::getName);
        sourceComboBox.setPlaceholder("Ингредиент или заготовка...");
        sourceComboBox.setClearButtonVisible(true);

        // Изначальные значения
        initAmountField.setValue(0.0);
        finalAmountField.setValue(0.0);
        lossesAmountField.setValue(0.0);
        lossesPercentField.setValue(0.0);

        // Минимальные значения = 0
        initAmountField.setMin(0);
        finalAmountField.setMin(0);
        lossesAmountField.setMin(0);
        lossesPercentField.setMin(0);

        // Ставим поля потерь в "read-only", чтобы пользователь не мог вручную их менять
        lossesAmountField.setReadOnly(true);
        lossesPercentField.setReadOnly(true);

        // Пересчитываем потери при изменении initAmount / finalAmount
        initAmountField.addValueChangeListener(e -> recalcLosses());
        finalAmountField.addValueChangeListener(e -> recalcLosses());

        // Обработчик кнопки «Добавить в рецепт»
        addButton.addClickListener(event -> {
            if (selectedPrepack == null) {
                Notification.show("Сначала выберите заготовку");
                return;
            }
            SourceDto chosenSource = sourceComboBox.getValue();
            if (chosenSource == null) {
                Notification.show("Выберите источник (ингредиент / заготовку)");
                return;
            }

            // Формируем DTO
            PrepackRecipeDto newRecipeDto = PrepackRecipeDto.builder()
                    .initAmount(initAmountField.getValue())
                    .finalAmount(finalAmountField.getValue())
                    .lossesAmount(lossesAmountField.getValue())
                    .lossesPercentage(lossesPercentField.getValue())
                    .build();

            // Сохраняем
            recipeService.savePrepackRecipe(newRecipeDto, chosenSource, selectedPrepack.getId());

            // Оповещаем пользователя
            Notification.show("Успешно добавлено!");

            // Очищаем поля формы
            clearAddForm();

            // Обновляем таблицу
            refreshRecipeGrid(selectedPrepack.getId());
        });
    }

    /**
     * Метод для пересчёта потерь и процента потерь
     */
    private void recalcLosses() {
        double init = initAmountField.getValue() != null ? initAmountField.getValue() : 0.0;
        double fin = finalAmountField.getValue() != null ? finalAmountField.getValue() : 0.0;

        double losses = init - fin;
        if (losses < 0) {
            // На всякий случай, если finalAmount > initAmount
            losses = 0.0;
        }

        double percentage = 0.0;
        if (init != 0) {
            percentage = (losses / init) * 100.0;
            // Округлим до сотых
            percentage = Math.round(percentage * 100.0) / 100.0;
        }

        lossesAmountField.setValue(losses);
        lossesPercentField.setValue(percentage);
    }

    /**
     * Создаём макет (layout) формы добавления
     */
    private VerticalLayout createAddFormLayout() {
        HorizontalLayout fieldsLayout = new HorizontalLayout(
                sourceComboBox,
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
     * Включает/выключает возможность добавления ингредиентов в рецепт
     */
    private void enableAddForm(boolean enable) {
        sourceComboBox.setEnabled(enable);
        initAmountField.setEnabled(enable);
        finalAmountField.setEnabled(enable);
        // Хотя поля потерь в read-only,
        // всё равно отключим их, чтобы они выглядели заблокированными
        // до выбора заготовки
        lossesAmountField.setEnabled(enable);
        lossesPercentField.setEnabled(enable);
        addButton.setEnabled(enable);
    }

    private void clearAddForm() {
        sourceComboBox.clear();
        initAmountField.setValue(0.0);
        finalAmountField.setValue(0.0);
        lossesAmountField.setValue(0.0);
        lossesPercentField.setValue(0.0);
    }

    private void refreshRecipeGrid(Long prepackId) {
        List<PrepackRecipeDto> recipeList =
                recipeService.getPrepackRecipeByPrepackId(prepackId);
        recipeGrid.setItems(recipeList);
    }
}
