package ru.sushi.delivery.kds.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import ru.sushi.delivery.kds.domain.controller.dto.IngredientDto;
import ru.sushi.delivery.kds.domain.persist.entity.Measurement;
import ru.sushi.delivery.kds.domain.service.IngredientService;
import ru.sushi.delivery.kds.domain.service.MeasurementService;

import java.time.Duration;
import java.util.List;

@Route(value = "ingredients")
@PageTitle("Ингредиенты | Доставка Суши")
@PermitAll
public class IngredientView extends VerticalLayout {

    private final IngredientService ingredientService;

    private final ComboBox<Measurement> measurementUnitField = new ComboBox<>("Единица измерения");
    private final MeasurementService measurementService;
    private final Grid<IngredientDto> ingredientGrid = new Grid<>();

    private final TextField nameField = new TextField("Название");
    private final NumberField pieceInGramsField = new NumberField("Количество в граммах");
    private final NumberField expirationDaysField = new NumberField("Срок годности (дни)");
    private final NumberField expirationHoursField = new NumberField("Срок годности (часы)");
    private final NumberField notifyAfterAmountField = new NumberField("Уведомить при остатке");

    @Autowired
    public IngredientView(IngredientService ingredientService, MeasurementService measurementService) {
        this.ingredientService = ingredientService;
        this.measurementService = measurementService;

        setSizeFull();

        configureGrid();
        add(createForm(), ingredientGrid);

        updateGrid();
    }

    private FormLayout createForm() {
        nameField.setPlaceholder("Введите название");
        notifyAfterAmountField.setPlaceholder("Введите количество для уведомления");
        pieceInGramsField.setPlaceholder("Введите количество в граммах");
        expirationDaysField.setPlaceholder("Введите количество дней");
        expirationHoursField.setPlaceholder("Введите количество часов");

        // Поле изначально отключено
        pieceInGramsField.setEnabled(false);

        List<Measurement> measurements = measurementService.getAll();
        measurementUnitField.setItems(measurements);
        measurementUnitField.setItemLabelGenerator(Measurement::getName);
        measurementUnitField.setPlaceholder("Выберите единицу измерения");

        // Слушатель для активации/деактивации поля Piece in Grams
        measurementUnitField.addValueChangeListener(event -> {
            Measurement selectedMeasurement = event.getValue();
            if (selectedMeasurement != null && selectedMeasurement.getId() == 2) { // ID = 2
                pieceInGramsField.setEnabled(true);
            }
            else {
                pieceInGramsField.setEnabled(false);
                pieceInGramsField.clear();
            }
        });

        Button addButton = new Button("Добавить ингредиент", e -> saveIngredient());
        addButton.getStyle().set("min-width", "150px"); // Минимальная ширина кнопки

        // Создаём FormLayout
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),  // Все компоненты в один столбец
                new FormLayout.ResponsiveStep("600px", 2),  // Два компонента в ряд
                new FormLayout.ResponsiveStep("900px", 3)   // Три компонента в ряд
        );

        // Добавляем компоненты в FormLayout
        formLayout.add(
                nameField,
                expirationDaysField,
                expirationHoursField,
                notifyAfterAmountField,
                measurementUnitField,
                pieceInGramsField,
                addButton
        );

        // Задаём выравнивание кнопки
        formLayout.setColspan(addButton, 3); // Растягиваем кнопку на 3 столбца

        return formLayout;
    }

    private void configureGrid() {
        ingredientGrid.setSizeFull();

        ingredientGrid.addColumn(IngredientDto::getId)
                .setHeader("ID")
                .setSortable(true)
                .setClassNameGenerator(item -> "text-center");

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
    }


    private void saveIngredient() {
        String name = nameField.getValue();
        Long pieceInGrams = pieceInGramsField.getValue() != null ? pieceInGramsField.getValue().longValue() : null;
        Long expirationDays = expirationDaysField.getValue() != null ? expirationDaysField.getValue().longValue() : 0;
        Long expirationHours = expirationHoursField.getValue() != null ? expirationHoursField.getValue().longValue() : 0;
        double notifyAfterAmount = notifyAfterAmountField.getValue() != null ? notifyAfterAmountField.getValue() : 0.0;
        Measurement selectedMeasurement = measurementUnitField.getValue();

        if (name == null || name.isEmpty() || selectedMeasurement == null) {
            Notification.show("Название и единица измерения обязательны для заполнения!");
            return;
        }

        Duration expirationDuration = Duration.ofDays(expirationDays).plusHours(expirationHours);

        IngredientDto newIngredient = IngredientDto.builder()
                .name(name)
                .pieceInGrams(pieceInGrams)
                .expirationDuration(expirationDuration)
                .notifyAfterAmount(notifyAfterAmount)
                .measurementUnitName(selectedMeasurement.getName())
                .build();

        ingredientService.saveIngredient(newIngredient);
        Notification.show("Ингредиент добавлен!");
        clearForm();
        updateGrid();
    }


    private void clearForm() {
        nameField.clear();
        pieceInGramsField.clear();
        expirationDaysField.clear();
        expirationHoursField.clear();
        notifyAfterAmountField.clear();
    }

    private void updateGrid() {
        List<IngredientDto> ingredients = ingredientService.getAllIngredients();
        ingredientGrid.setItems(ingredients);
    }

    private String formatDuration(Duration duration) {
        if (duration == null) {
            return "Не указано";
        }
        long days = duration.toDays();
        long hours = duration.toHours() % 24; // Остаток часов после дней
        return days > 0 ? String.format("%d дней %d часов", days, hours) : String.format("%d часов", hours);
    }
}
