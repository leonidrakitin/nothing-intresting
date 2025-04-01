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
import ru.sushi.delivery.kds.domain.controller.dto.PrepackData;
import ru.sushi.delivery.kds.domain.persist.entity.Measurement;
import ru.sushi.delivery.kds.domain.persist.entity.recipe.Recipe;
import ru.sushi.delivery.kds.domain.service.MeasurementService;
import ru.sushi.delivery.kds.domain.service.PrepackService;
import ru.sushi.delivery.kds.domain.service.RecipeService;
import ru.sushi.delivery.kds.domain.service.SourceService;
import ru.sushi.delivery.kds.model.SourceType;

import java.time.Duration;
import java.util.List;

@Route(value = "prepacks")
@PageTitle("ПФки | Доставка Суши")
@PermitAll
public class PrepackView extends VerticalLayout {

    private final PrepackService prepackService;
    private final MeasurementService measurementService;
    private final SourceService sourceService;
    private final RecipeService recipeService;

    private final Grid<PrepackData> prepackGrid = new Grid<>();

    private final TextField nameField = new TextField("Название");
    private final NumberField expirationDaysField = new NumberField("Срок годности (дни)");
    private final NumberField expirationHoursField = new NumberField("Срок годности (часы)");
    private final NumberField notifyAfterAmountField = new NumberField("Уведомить при остатке");
    private final ComboBox<Measurement> measurementUnitField = new ComboBox<>("Единица измерения");

    // Кнопка, которая переключается между "Добавить" и "Изменить"
    private final Button saveButton = new Button("Добавить новый ПФ");
    // Кнопка "Отменить изменения"
    private final Button cancelButton = new Button("Отменить изменения");

    // Текущий ПФ, который редактируем (если null — значит режим добавления)
    private PrepackData currentEditingPrepack = null;

    @Autowired
    public PrepackView(PrepackService prepackService, MeasurementService measurementService, SourceService sourceService, RecipeService recipeService) {
        this.prepackService = prepackService;
        this.measurementService = measurementService;
        this.sourceService = sourceService;
        this.recipeService = recipeService;

        setSizeFull();

        configureGrid();
        add(prepackGrid, createForm());

        updateGrid();
    }

    /**
     * Настраиваем Grid для отображения списка ПФ + добавляем колонку действий
     */
    private void configureGrid() {
        prepackGrid.setSizeFull();

        // Основные колонки
        prepackGrid.addColumn(PrepackData::getId)
                .setHeader("ID")
                .setSortable(true)
                .setClassNameGenerator(item -> "text-center");

        prepackGrid.addColumn(PrepackData::getName)
                .setHeader("Название")
                .setSortable(true)
                .setClassNameGenerator(item -> "text-center");

        prepackGrid.addColumn(PrepackData::getMeasurementUnitName)
                .setHeader("Единица измерения")
                .setSortable(true)
                .setClassNameGenerator(item -> "text-center");

        prepackGrid.addColumn(dto -> formatDuration(dto.getExpirationDuration()))
                .setHeader("Срок годности")
                .setSortable(true)
                .setClassNameGenerator(item -> "text-center");

        prepackGrid.addColumn(PrepackData::getNotifyAfterAmount)
                .setHeader("Уведомить при остатке")
                .setSortable(true)
                .setClassNameGenerator(item -> "text-center");

        prepackGrid.addColumn(prepack -> String.format("%.2f руб", prepack.getFcPrice()))
                .setHeader("Себестоимость за 1кг")
                .setSortable(true)
                .setClassNameGenerator(item -> "text-center");

        // Колонка с кнопками "Изменить" и "Удалить"
        prepackGrid.addComponentColumn(this::createActionButtons)
                .setHeader("Действия")
                .setClassNameGenerator(item -> "text-center");
    }

    /**
     * Создаём горизонтальный лейаут с кнопками "Изменить" и "Удалить" для каждой строки
     */
    private HorizontalLayout createActionButtons(PrepackData prepack) {
        Button editButton = new Button("Изменить", event -> loadPrepackIntoForm(prepack));
        Button deleteButton = new Button("Удалить", event -> deletePrepack(prepack));

        editButton.getStyle().set("margin-right", "0.5em");
        return new HorizontalLayout(editButton, deleteButton);
    }

    /**
     * Загружаем данные выбранного ПФ в форму (режим редактирования)
     */
    private void loadPrepackIntoForm(PrepackData prepack) {
        this.currentEditingPrepack = prepack;

        // Поля
        nameField.setValue(prepack.getName() != null ? prepack.getName() : "");
        if (prepack.getExpirationDuration() != null) {
            long days = prepack.getExpirationDuration().toDays();
            long hours = prepack.getExpirationDuration().toHours() % 24;
            expirationDaysField.setValue((double) days);
            expirationHoursField.setValue((double) hours);
        }
        else {
            expirationDaysField.clear();
            expirationHoursField.clear();
        }
        notifyAfterAmountField.setValue(prepack.getNotifyAfterAmount() != null
                ? prepack.getNotifyAfterAmount()
                : 0.0
        );

        // Measurement
        Measurement measurement = measurementService.getAll().stream()
                .filter(m -> m.getName().equals(prepack.getMeasurementUnitName()))
                .findFirst()
                .orElse(null);
        measurementUnitField.setValue(measurement);

        // Меняем текст кнопки
        saveButton.setText("Изменить ПФ");
        // Делаем кнопку «Отменить» видимой
        cancelButton.setVisible(true);
    }

    /**
     * Создаём форму для создания/редактирования Prepack
     */
    private FormLayout createForm() {
        nameField.setPlaceholder("Введите название");
        notifyAfterAmountField.setPlaceholder("Введите количество для уведомления");
        expirationDaysField.setPlaceholder("Введите количество дней");
        expirationHoursField.setPlaceholder("Введите количество часов");

        // Заполняем ComboBox единицами измерения
        List<Measurement> measurements = measurementService.getAll();
        measurementUnitField.setItems(measurements);
        measurementUnitField.setItemLabelGenerator(Measurement::getName);
        measurementUnitField.setPlaceholder("Выберите единицу измерения");

        // Кнопка «Отменить изменения» изначально скрыта
        cancelButton.setVisible(false);
        cancelButton.addClickListener(e -> clearForm());

        // Кнопка «Добавить/Изменить»
        saveButton.addClickListener(e -> {
            if (currentEditingPrepack == null) {
                // Режим добавления
                createOrUpdatePrepack(null);
            }
            else {
                // Режим редактирования
                createOrUpdatePrepack(currentEditingPrepack.getId());
            }
        });
        saveButton.getStyle().set("min-width", "150px");

        // Формируем layout
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),    // все компоненты в один столбец
                new FormLayout.ResponsiveStep("600px", 2),
                new FormLayout.ResponsiveStep("900px", 3)
        );

        // Добавляем поля и кнопки
        formLayout.add(
                nameField,
                expirationDaysField,
                expirationHoursField,
                notifyAfterAmountField,
                measurementUnitField,
                new HorizontalLayout(saveButton, cancelButton)
        );

        // (Необязательно) Если хотите растянуть кнопки на все 3 столбца:
        // formLayout.setColspan(formLayout.getComponent(formLayout.getComponentCount()-1), 3);

        return formLayout;
    }

    /**
     * Создаём или обновляем ПФ (если передан id != null, значит обновляем)
     */
    private void createOrUpdatePrepack(Long id) {
        String name = nameField.getValue();
        Measurement selectedMeasurement = measurementUnitField.getValue();

        Long expirationDays = expirationDaysField.getValue() != null
                ? expirationDaysField.getValue().longValue()
                : 0;
        Long expirationHours = expirationHoursField.getValue() != null
                ? expirationHoursField.getValue().longValue()
                : 0;

        double notifyAfterAmount = notifyAfterAmountField.getValue() != null
                ? notifyAfterAmountField.getValue()
                : 0.0;

        if (name == null || name.isEmpty() || selectedMeasurement == null) {
            Notification.show("Название и единица измерения обязательны для заполнения!");
            return;
        }

        Duration expirationDuration = Duration.ofDays(expirationDays).plusHours(expirationHours);

        // Собираем DTO
        PrepackData prepackData = PrepackData.builder()
                .id(id)
                .name(name)
                .measurementUnitName(selectedMeasurement.getName())
                .expirationDuration(expirationDuration)
                .notifyAfterAmount(notifyAfterAmount)
                .build();

        prepackService.savePrepack(prepackData);

        Notification.show(id == null ? "ПФ добавлен!" : "Изменения сохранены!");

        clearForm();
        updateGrid();
    }

    /**
     * Удаляем ПФ
     */
    private void deletePrepack(PrepackData prepack) {
        if (prepack.getId() == null) {
            Notification.show("Не удалось удалить: отсутствует ID.");
            return;
        }

        List<Recipe> recipes = recipeService.checkRecipeDependencies(prepack, SourceType.PREPACK);
        if (recipes.isEmpty()) {
            prepackService.deletePrepack(prepack);
            Notification.show("Строка удалёна!");
            updateGrid();
        }
        else {
            for (Recipe recipe : recipes) {
                String name = sourceService.getSourceName(recipe.getSourceId(), recipe.getSourceType());
                Notification.show("Невозможно удалить пф содержится в " + name);
            }
        }

        // Если удалили тот ПФ, который редактировали — сбросим форму
        if (currentEditingPrepack != null && currentEditingPrepack.getId().equals(prepack.getId())) {
            clearForm();
        }
    }

    /**
     * Очищаем форму и возвращаемся в режим добавления
     */
    private void clearForm() {
        nameField.clear();
        expirationDaysField.clear();
        expirationHoursField.clear();
        notifyAfterAmountField.clear();
        measurementUnitField.clear();

        currentEditingPrepack = null;
        saveButton.setText("Добавить новый ПФ");
        cancelButton.setVisible(false);
    }

    /**
     * Подгружаем список ПФ в таблицу
     */
    private void updateGrid() {
        List<PrepackData> prepacks = prepackService.getAllPrepacks();
        prepackGrid.setItems(prepacks);
    }

    /**
     * Форматируем Duration в человекочитаемый вид
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
