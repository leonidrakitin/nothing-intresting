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
import ru.sushi.delivery.kds.domain.controller.dto.MealData;
import ru.sushi.delivery.kds.domain.persist.entity.flow.Flow;
import ru.sushi.delivery.kds.domain.persist.repository.flow.FlowRepository;
import ru.sushi.delivery.kds.domain.service.MealService;

import java.time.Duration;
import java.util.List;

@Route(value = "menu-items")
@PageTitle("Пункты меню | Доставка Суши")
@PermitAll
public class MealView extends VerticalLayout {

    private final MealService mealService;
    private final FlowRepository flowRepository;

    private final Grid<MealData> mealGrid = new Grid<>();

    private final TextField nameField = new TextField("Название");
    private final ComboBox<Flow> flowComboBox = new ComboBox<>("Отображение");
    private final NumberField timeToCookSecField = new NumberField("Время приготовления (сек)");
    private final NumberField priceField = new NumberField("Цена (рублей)");

    // Кнопка, которая переключается между "Добавить" и "Изменить"
    private final Button saveButton = new Button("Добавить пункт меню");
    // Кнопка "Отменить изменения"
    private final Button cancelButton = new Button("Отменить изменения");

    // Текущий пункт меню, который редактируем (если null — значит режим добавления)
    private MealData currentEditingMeal = null;

    @Autowired
    public MealView(MealService mealService, FlowRepository flowRepository) {
        this.mealService = mealService;
        this.flowRepository = flowRepository;

        setSizeFull();

        configureGrid();
        add(createForm(), mealGrid);

        updateGrid();
    }

    /**
     * Настраиваем Grid: колонки + колонка "Действия" (Изменить/Удалить).
     */
    private void configureGrid() {
        mealGrid.setSizeFull();

        mealGrid.addColumn(MealData::getId)
                .setHeader("ID")
                .setSortable(true)
                .setClassNameGenerator(item -> "text-center");

        mealGrid.addColumn(MealData::getName)
                .setHeader("Название")
                .setSortable(true)
                .setClassNameGenerator(item -> "text-center");

        mealGrid.addColumn(MealData::getPrice)
                .setHeader("Цена")
                .setSortable(true)
                .setClassNameGenerator(item -> "text-center");

        mealGrid.addColumn(MealData::getFlow)
                .setHeader("Отображение")
                .setSortable(true)
                .setClassNameGenerator(item -> "text-center");

        mealGrid.addColumn(item -> item.getTimeToCook().toSeconds())
                .setHeader("Время готовки (сек)")
                .setSortable(true)
                .setClassNameGenerator(item -> "text-center");

        // Добавляем колонку «Действия»
        mealGrid.addComponentColumn(this::createActionButtons)
                .setHeader("Действия")
                .setClassNameGenerator(item -> "text-center");
    }

    /**
     * Создаём горизонтальный лэйаут с кнопками "Изменить" и "Удалить"
     */
    private HorizontalLayout createActionButtons(MealData meal) {
        Button editButton = new Button("Изменить", event -> {
            loadMealIntoForm(meal);
        });

        Button deleteButton = new Button("Удалить", event -> {
            deleteMeal(meal);
        });

        editButton.getStyle().set("margin-right", "0.5em");
        return new HorizontalLayout(editButton, deleteButton);
    }

    /**
     * Загрузка выбранного пункта меню в поля формы (режим редактирования).
     */
    private void loadMealIntoForm(MealData meal) {
        this.currentEditingMeal = meal;

        // Заполняем поля
        nameField.setValue(meal.getName() != null ? meal.getName() : "");
        // Найдём Flow по названию из DTO (meal.getFlow()) и выставим в ComboBox
        if (meal.getFlow() != null) {
            Flow flow = flowRepository.findAll().stream()
                    .filter(f -> f.getName().equals(meal.getFlow()))
                    .findFirst()
                    .orElse(null);
            flowComboBox.setValue(flow);
            timeToCookSecField.setValue((double) meal.getTimeToCook().toSeconds());
            priceField.setValue(meal.getPrice());
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
            if (currentEditingMeal == null) {
                // Режим добавления
                createOrUpdateMeal(null);
            }
            else {
                // Режим редактирования
                createOrUpdateMeal(currentEditingMeal.getId());
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
    private void createOrUpdateMeal(Long id) {
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
        MealData mealData = MealData.builder()
                .id(id)
                .name(name)
                .flow(selectedFlow.getName())
                .timeToCook(timeToCookDuration)
                .price(priceField.getValue())
                .build();

        // Сохраняем (сервис сам определит create/update по наличию id)
        mealService.saveMeal(mealData);

        Notification.show(id == null ? "Пункт меню успешно добавлен!" : "Изменения сохранены!");
        clearForm();
        updateGrid();
    }

    /**
     * Удаляем пункт меню
     */
    private void deleteMeal(MealData meal) {
        if (meal.getId() == null) {
            Notification.show("Не удалось удалить: отсутствует ID пункта меню.");
            return;
        }
        mealService.deleteMeal(meal);
        Notification.show("Пункт меню удалён!");
        updateGrid();

        // Если удалили тот, который редактировали, сбрасываем форму
        if (currentEditingMeal != null && currentEditingMeal.getId().equals(meal.getId())) {
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
        currentEditingMeal = null;
        saveButton.setText("Добавить пункт меню");
        cancelButton.setVisible(false);
    }

    /**
     * Обновляет данные в Grid
     */
    private void updateGrid() {
        List<MealData> meals = mealService.getAllMealsDTO();
        mealGrid.setItems(meals);
    }
}
