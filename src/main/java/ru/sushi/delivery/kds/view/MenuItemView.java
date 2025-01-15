package ru.sushi.delivery.kds.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import ru.sushi.delivery.kds.domain.controller.dto.MenuItemDto;
import ru.sushi.delivery.kds.domain.persist.entity.flow.Flow;
import ru.sushi.delivery.kds.domain.persist.repository.flow.FlowRepository;
import ru.sushi.delivery.kds.domain.service.MenuItemService;

import java.util.List;

@Route(value = "menu-items")
@PageTitle("Пункты меню | Доставка Суши")
@PermitAll
public class MenuItemView extends VerticalLayout {

    private final MenuItemService menuItemService;
    private final FlowRepository flowRepository;

    private final Grid<MenuItemDto> menuItemGrid = new Grid<>();

    private final TextField nameField = new TextField("Название");
    private final ComboBox<Flow> flowComboBox = new ComboBox<>("Поток");

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
     * Создаёт форму для создания/редактирования MenuItem
     */
    private FormLayout createForm() {
        nameField.setPlaceholder("Введите название пункта меню");

        // Список всех доступных потоков (Flow)
        List<Flow> flows = flowRepository.findAll();
        flowComboBox.setItems(flows);
        flowComboBox.setItemLabelGenerator(Flow::getName);
        flowComboBox.setPlaceholder("Выберите поток");

        Button addButton = new Button("Добавить пункт меню", event -> saveMenuItem());
        addButton.getStyle().set("min-width", "150px"); // Минимальная ширина кнопки

        // Создаём FormLayout
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),   // Все компоненты в один столбец
                new FormLayout.ResponsiveStep("600px", 2) // Два компонента в ряд (по желанию можно добавить и третий шаг)
        );

        // Добавляем компоненты в FormLayout
        formLayout.add(
                nameField,
                flowComboBox,
                addButton
        );

        // Пример: Растягиваем кнопку на 2 столбца (если нужно)
        formLayout.setColspan(addButton, 2);

        return formLayout;
    }

    /**
     * Настраиваем Grid для отображения списка пунктов меню
     */
    private void configureGrid() {
        menuItemGrid.setSizeFull();

        menuItemGrid.addColumn(MenuItemDto::getId)
                .setHeader("ID")
                .setSortable(true)
                .setClassNameGenerator(item -> "text-center");

        menuItemGrid.addColumn(MenuItemDto::getName)
                .setHeader("Название")
                .setSortable(true)
                .setClassNameGenerator(item -> "text-center");

        menuItemGrid.addColumn(MenuItemDto::getFlow)
                .setHeader("Поток")
                .setSortable(true)
                .setClassNameGenerator(item -> "text-center");
    }

    /**
     * Сохраняет новый пункт меню, введённый в форму
     */
    private void saveMenuItem() {
        String name = nameField.getValue();
        Flow selectedFlow = flowComboBox.getValue();

        // Проверяем обязательные поля
        if (name == null || name.isEmpty() || selectedFlow == null) {
            Notification.show("Название и поток обязательны для заполнения!");
            return;
        }

        // Создаём DTO
        MenuItemDto menuItemDTO = MenuItemDto.builder()
                .name(name)
                .flow(selectedFlow.getName()) // Сохраняем название потока
                .build();

        // Сохраняем через сервис
        menuItemService.saveMenuItem(menuItemDTO);

        Notification.show("Пункт меню успешно добавлен!");
        clearForm();
        updateGrid();
    }

    /**
     * Очищает поля формы
     */
    private void clearForm() {
        nameField.clear();
        flowComboBox.clear();
    }

    /**
     * Обновляет данные в Grid
     */
    private void updateGrid() {
        List<MenuItemDto> menuItems = menuItemService.getAllMenuItemsDTO();
        menuItemGrid.setItems(menuItems);
    }
}
