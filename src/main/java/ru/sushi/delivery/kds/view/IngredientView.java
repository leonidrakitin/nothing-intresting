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
import ru.sushi.delivery.kds.domain.controller.dto.IngredientDto;
import ru.sushi.delivery.kds.domain.persist.entity.Measurement;
import ru.sushi.delivery.kds.domain.persist.entity.recipe.Recipe;
import ru.sushi.delivery.kds.domain.service.IngredientService;
import ru.sushi.delivery.kds.domain.service.MeasurementService;
import ru.sushi.delivery.kds.domain.service.RecipeService;
import ru.sushi.delivery.kds.domain.service.SourceService;
import ru.sushi.delivery.kds.model.SourceType;

import java.time.Duration;
import java.util.List;

@Route(value = "ingredients")
@PageTitle("Ингредиенты | Доставка Суши")
@PermitAll
public class IngredientView extends VerticalLayout {

    private final IngredientService ingredientService;
    private final MeasurementService measurementService;
    private final RecipeService recipeService;
    private final SourceService sourceService;

    private final Grid<IngredientDto> ingredientGrid = new Grid<>();
    private final ComboBox<Measurement> measurementUnitField = new ComboBox<>("Единица измерения");

    private final TextField nameField = new TextField("Название");
    private final NumberField pieceInGramsField = new NumberField("Количество в граммах");
    private final NumberField expirationDaysField = new NumberField("Срок годности (дни)");
    private final NumberField expirationHoursField = new NumberField("Срок годности (часы)");
    private final NumberField notifyAfterAmountField = new NumberField("Уведомить при остатке");

    // Кнопка, которая будет переключаться между "Добавить" и "Изменить"
    private final Button saveButton = new Button("Добавить новый ингредиент");
    // Кнопка "Отменить изменение"
    private final Button cancelButton = new Button("Отменить изменения");

    // Храним текущий ингредиент, который редактируем
    private IngredientDto currentEditingIngredient = null;

    @Autowired
    public IngredientView(IngredientService ingredientService, MeasurementService measurementService, RecipeService recipeService, SourceService sourceService) {
        this.ingredientService = ingredientService;
        this.measurementService = measurementService;
        this.recipeService = recipeService;
        this.sourceService = sourceService;

        setSizeFull();

        configureGrid();
        add(ingredientGrid, createForm());

        updateGrid();
    }

    /**
     * Настраиваем таблицу и добавляем колонки, включая "Изменить"/"Удалить"
     */
    private void configureGrid() {
        ingredientGrid.setSizeFull();

        ingredientGrid.addColumn(IngredientDto::getName)
                .setHeader("Название")
                .setSortable(true)
                .setClassNameGenerator(item -> "text-center");

        ingredientGrid.addColumn(IngredientDto::getMeasurementUnitName)
                .setHeader("Единица измерения")
                .setSortable(true)
                .setClassNameGenerator(item -> "text-center");

        ingredientGrid.addColumn(dto -> dto.getPieceInGrams() != null ? dto.getPieceInGrams() : "-")
                .setHeader("Количество в граммах")
                .setSortable(true)
                .setClassNameGenerator(item -> "text-center");

        ingredientGrid.addColumn(dto -> formatDuration(dto.getExpirationDuration()))
                .setHeader("Срок годности")
                .setSortable(true)
                .setClassNameGenerator(item -> "text-center");

        ingredientGrid.addColumn(IngredientDto::getNotifyAfterAmount)
                .setHeader("Уведомить при остатке")
                .setSortable(true)
                .setClassNameGenerator(item -> "text-center");

        // Добавляем колонку "Действия" с кнопками "Изменить" и "Удалить"
        ingredientGrid.addComponentColumn(this::createActionButtons)
                .setHeader("Действия")
                .setClassNameGenerator(item -> "text-center");
    }

    /**
     * Создаём горизонтальный лэйаут с кнопками "Изменить" и "Удалить"
     */
    private HorizontalLayout createActionButtons(IngredientDto ingredient) {
        Button editButton = new Button("Изменить", event -> {
            // Нажатие на "Изменить" -> заполняем поля формы
            loadIngredientIntoForm(ingredient);
        });

        Button deleteButton = new Button("Удалить", event -> {
            // Нажатие на "Удалить" -> удаляем ингредиент
            this.deleteIngredient(ingredient);
        });

        editButton.getStyle().set("margin-right", "0.5em");
        return new HorizontalLayout(editButton, deleteButton);
    }

    /**
     * Загружаем выбранный ингредиент в поля формы и переключаемся в "режим редактирования"
     */
    private void loadIngredientIntoForm(IngredientDto ingredient) {
        currentEditingIngredient = ingredient;

        nameField.setValue(ingredient.getName() != null ? ingredient.getName() : "");
        pieceInGramsField.setValue(ingredient.getPieceInGrams() != null ? ingredient.getPieceInGrams().doubleValue() : 0.0);
        if (ingredient.getExpirationDuration() != null) {
            long days = ingredient.getExpirationDuration().toDays();
            long hours = ingredient.getExpirationDuration().toHours() % 24;
            expirationDaysField.setValue((double) days);
            expirationHoursField.setValue((double) hours);
        }
        else {
            expirationDaysField.clear();
            expirationHoursField.clear();
        }
        notifyAfterAmountField.setValue(ingredient.getNotifyAfterAmount() != null ? ingredient.getNotifyAfterAmount() : 0.0);

        // Нужно снова найти Measurement по названию (если нужно)
        Measurement measurement = measurementService.getAll().stream()
                .filter(m -> m.getName().equals(ingredient.getMeasurementUnitName()))
                .findFirst()
                .orElse(null);
        measurementUnitField.setValue(measurement);

        // Если measurement.getId() == 2 (например, "Штука") - то поле pieceInGramsField включаем
        pieceInGramsField.setEnabled(measurement != null && measurement.getId() == 2);

        // Меняем текст на кнопке
        saveButton.setText("Изменить ингредиент");
        // Показываем кнопку "Отменить изменения"
        cancelButton.setVisible(true);
    }

    /**
     * Создаём форму ввода данных для ингредиента
     */
    private FormLayout createForm() {
        nameField.setPlaceholder("Введите название");
        notifyAfterAmountField.setPlaceholder("Введите количество для уведомления");
        pieceInGramsField.setPlaceholder("Введите количество в граммах");
        expirationDaysField.setPlaceholder("Введите количество дней");
        expirationHoursField.setPlaceholder("Введите количество часов");

        // Поле изначально отключено (включаем только для определённой Measurement, например "Штука")
        pieceInGramsField.setEnabled(false);

        // Загрузка всех единиц измерения
        List<Measurement> measurements = measurementService.getAll();
        measurementUnitField.setItems(measurements);
        measurementUnitField.setItemLabelGenerator(Measurement::getName);
        measurementUnitField.setPlaceholder("Выберите единицу измерения");

        // Слушатель для активации/деактивации поля pieceInGramsField
        measurementUnitField.addValueChangeListener(event -> {
            Measurement selectedMeasurement = event.getValue();
            if (selectedMeasurement != null && selectedMeasurement.getId() == 2) {
                pieceInGramsField.setEnabled(true);
            }
            else {
                pieceInGramsField.setEnabled(false);
                pieceInGramsField.clear();
            }
        });

        // По умолчанию "Отменить изменения" скрыта
        cancelButton.setVisible(false);
        cancelButton.addClickListener(e -> {
            // Нажатие на «Отменить изменения» -> возвращаемся в режим добавления
            clearForm();
        });

        // Обработчик нажатия на кнопку "Добавить" или "Изменить"
        saveButton.addClickListener(e -> {
            if (currentEditingIngredient == null) {
                // Режим добавления
                createOrUpdateIngredient(null);
            }
            else {
                // Режим редактирования - у нас есть currentEditingIngredient
                createOrUpdateIngredient(currentEditingIngredient.getId());
            }
        });
        saveButton.getStyle().set("min-width", "150px"); // Минимальная ширина кнопки

        // Создаём FormLayout
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),   // Все компоненты в один столбец
                new FormLayout.ResponsiveStep("600px", 2),
                new FormLayout.ResponsiveStep("900px", 3)
        );

        // Добавляем компоненты в FormLayout
        formLayout.add(
                nameField,
                expirationDaysField,
                expirationHoursField,
                notifyAfterAmountField,
                measurementUnitField,
                pieceInGramsField,
                new HorizontalLayout(saveButton, cancelButton)
        );

        // Задаём выравнивание кнопки
//        formLayout.setColspan(formLayout.getComponent(formLayout.getComponentCount()-1), 3);
        formLayout.setColspan(saveButton, 3); // Растягиваем кнопку на 3 столбца

        return formLayout;
    }

    /**
     * Создаём или обновляем ингредиент
     * (если передан id != null — режим редактирования)
     */
    private void createOrUpdateIngredient(Long id) {
        String name = nameField.getValue();
        Long pieceInGrams = pieceInGramsField.getValue() != null
                ? pieceInGramsField.getValue().longValue()
                : null;

        Long expirationDays = expirationDaysField.getValue() != null
                ? expirationDaysField.getValue().longValue()
                : 0;
        Long expirationHours = expirationHoursField.getValue() != null
                ? expirationHoursField.getValue().longValue()
                : 0;

        double notifyAfterAmount = notifyAfterAmountField.getValue() != null
                ? notifyAfterAmountField.getValue()
                : 0.0;

        Measurement selectedMeasurement = measurementUnitField.getValue();

        if (name == null || name.isEmpty() || selectedMeasurement == null) {
            Notification.show("Название и единица измерения обязательны для заполнения!");
            return;
        }

        // Формируем срок годности
        Duration expirationDuration = Duration.ofDays(expirationDays).plusHours(expirationHours);

        // Собираем Dto
        IngredientDto ingredientDto = IngredientDto.builder()
                .id(id) // если id=null, значит создаём новый; если нет — обновляем
                .name(name)
                .pieceInGrams(pieceInGrams)
                .expirationDuration(expirationDuration)
                .notifyAfterAmount(notifyAfterAmount)
                .measurementUnitName(selectedMeasurement.getName())
                .build();

        // Сервис с одним методом "saveIngredient" может сам понять, что делать (create/update).
        ingredientService.save(ingredientDto);

        Notification.show(id == null ? "Ингредиент добавлен!" : "Изменения сохранены!");

        // Очищаем форму
        clearForm();
        // Обновляем грид
        updateGrid();
    }

    /**
     * Удаляем ингредиент
     */
    private void deleteIngredient(IngredientDto ingredient) {
        if (ingredient.getId() == null) {
            Notification.show("Не удалось удалить: отсутствует ID ингредиента.");
            return;
        }


        List<Recipe> recipes = recipeService.checkRecipeDependencies(ingredient, SourceType.PREPACK);
        if (recipes.isEmpty()) {
            ingredientService.delete(ingredient);
            Notification.show("Ингредиент удалён!");
            updateGrid();
        }
        else {
            for (Recipe recipe : recipes) {
                String name = sourceService.getSourceItemName(recipe.getSourceId(), recipe.getSourceType());
                Notification.show("Невозможно удалить ингредиент содержится в " + name);
            }
        }

        // Если удалили тот ингредиент, который редактировали — сбросим форму
        if (currentEditingIngredient != null && currentEditingIngredient.getId().equals(ingredient.getId())) {
            clearForm();
        }
    }

    /**
     * Очищаем поля формы и возвращаемся в "режим добавления"
     */
    private void clearForm() {
        nameField.clear();
        pieceInGramsField.clear();
        expirationDaysField.clear();
        expirationHoursField.clear();
        notifyAfterAmountField.clear();
        measurementUnitField.clear();
        pieceInGramsField.setEnabled(false);

        // Возвращаем кнопку в состояние "Добавить новый ингредиент"
        currentEditingIngredient = null;
        saveButton.setText("Добавить новый ингредиент");
        cancelButton.setVisible(false); // Прячем кнопку "Отменить изменения"
    }

    /**
     * Подгружаем список ингредиентов в таблицу
     */
    private void updateGrid() {
        List<IngredientDto> ingredients = ingredientService.getAllIngredients();
        ingredientGrid.setItems(ingredients);
    }

    /**
     * Форматируем срок годности в человекочитаемый вид
     */
    private String formatDuration(Duration duration) {
        if (duration == null) {
            return "Не указано";
        }
        long days = duration.toDays();
        long hours = duration.toHours() % 24; // Остаток часов после дней
        if (days > 0) {
            return String.format("%d дн %d ч", days, hours);
        }
        return String.format("%d ч", hours);
    }
}
