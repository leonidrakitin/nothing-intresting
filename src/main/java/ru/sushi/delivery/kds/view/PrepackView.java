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
import ru.sushi.delivery.kds.domain.controller.dto.PrepackDto;
import ru.sushi.delivery.kds.domain.persist.entity.Measurement;
import ru.sushi.delivery.kds.domain.service.MeasurementService;
import ru.sushi.delivery.kds.domain.service.PrepackService;

import java.time.Duration;
import java.util.List;

@Route(value = "prepacks")
@PageTitle("ПФки | Доставка Суши")
@PermitAll
public class PrepackView extends VerticalLayout {

    private final PrepackService prepackService;
    private final MeasurementService measurementService;

    private final Grid<PrepackDto> prepackGrid = new Grid<>();

    private final TextField nameField = new TextField("Название");
    private final NumberField expirationDaysField = new NumberField("Срок годности (дни)");
    private final NumberField expirationHoursField = new NumberField("Срок годности (часы)");
    private final NumberField notifyAfterAmountField = new NumberField("Уведомить при остатке");
    private final ComboBox<Measurement> measurementUnitField = new ComboBox<>("Единица измерения");

    @Autowired
    public PrepackView(PrepackService prepackService, MeasurementService measurementService) {
        this.prepackService = prepackService;
        this.measurementService = measurementService;

        setSizeFull();

        configureGrid();
        add(createForm(), prepackGrid);

        updateGrid();
    }

    /**
     * Создаёт форму для создания/редактирования Prepack
     */
    private FormLayout createForm() {
        nameField.setPlaceholder("Введите название");
        notifyAfterAmountField.setPlaceholder("Введите количество для уведомления");
        expirationDaysField.setPlaceholder("Введите количество дней");
        expirationHoursField.setPlaceholder("Введите количество часов");

        // Настройка выпадающего списка с единицами измерения
        List<Measurement> measurements = measurementService.getAll();
        measurementUnitField.setItems(measurements);
        measurementUnitField.setItemLabelGenerator(Measurement::getName);
        measurementUnitField.setPlaceholder("Выберите единицу измерения");

        Button addButton = new Button("Добавить ПФ", e -> savePrepack());
        addButton.getStyle().set("min-width", "150px"); // Минимальная ширина кнопки

        // Создаём FormLayout
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),   // Все компоненты в один столбец
                new FormLayout.ResponsiveStep("600px", 2), // Два компонента в ряд
                new FormLayout.ResponsiveStep("900px", 3)  // Три компонента в ряд
        );

        // Добавляем компоненты в FormLayout
        formLayout.add(
                nameField,
                expirationDaysField,
                expirationHoursField,
                notifyAfterAmountField,
                measurementUnitField,
                addButton
        );

        // Растягиваем кнопку на всю ширину (3 столбца)
        formLayout.setColspan(addButton, 3);

        return formLayout;
    }

    /**
     * Настраиваем Grid для отображения списка ПФ
     */
    private void configureGrid() {
        prepackGrid.setSizeFull();

        prepackGrid.addColumn(PrepackDto::getId)
                .setHeader("ID")
                .setSortable(true)
                .setClassNameGenerator(item -> "text-center");

        prepackGrid.addColumn(PrepackDto::getName)
                .setHeader("Название")
                .setSortable(true)
                .setClassNameGenerator(item -> "text-center");

        prepackGrid.addColumn(PrepackDto::getMeasurementUnitName)
                .setHeader("Единица измерения")
                .setSortable(true)
                .setClassNameGenerator(item -> "text-center");

        prepackGrid.addColumn(dto -> formatDuration(dto.getExpirationDuration()))
                .setHeader("Срок годности")
                .setSortable(true)
                .setClassNameGenerator(item -> "text-center");

        prepackGrid.addColumn(PrepackDto::getNotifyAfterAmount)
                .setHeader("Уведомить при остатке")
                .setSortable(true)
                .setClassNameGenerator(item -> "text-center");
    }

    /**
     * Сохраняет ПФ, введённый в форму
     */
    private void savePrepack() {
        String name = nameField.getValue();
        Measurement selectedMeasurement = measurementUnitField.getValue();
        Long expirationDays = expirationDaysField.getValue() != null ? expirationDaysField.getValue().longValue() : 0;
        Long expirationHours = expirationHoursField.getValue() != null ? expirationHoursField.getValue().longValue() : 0;
        double notifyAfterAmount = notifyAfterAmountField.getValue() != null ? notifyAfterAmountField.getValue() : 0.0;

        if (name == null || name.isEmpty() || selectedMeasurement == null) {
            Notification.show("Название и единица измерения обязательны для заполнения!");
            return;
        }

        Duration expirationDuration = Duration.ofDays(expirationDays).plusHours(expirationHours);

        PrepackDto prepackDTO = PrepackDto.builder()
                .name(name)
                .measurementUnitName(selectedMeasurement.getName())
                .expirationDuration(expirationDuration)
                .notifyAfterAmount(notifyAfterAmount)
                .build();

        prepackService.savePrepack(prepackDTO);

        Notification.show("ПФ добавлен!");
        clearForm();
        updateGrid();
    }

    /**
     * Очищает форму
     */
    private void clearForm() {
        nameField.clear();
        expirationDaysField.clear();
        expirationHoursField.clear();
        notifyAfterAmountField.clear();
        measurementUnitField.clear();
    }

    /**
     * Обновляет данные в Grid
     */
    private void updateGrid() {
        List<PrepackDto> prepacks = prepackService.getAllPrepacks();
        prepackGrid.setItems(prepacks);
    }

    /**
     * Форматирует Duration в человекочитаемый вид
     */
    private String formatDuration(Duration duration) {
        if (duration == null) {
            return "Не указано";
        }
        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        return days > 0
                ? String.format("%d дней %d часов", days, hours)
                : String.format("%d часов", hours);
    }
}
