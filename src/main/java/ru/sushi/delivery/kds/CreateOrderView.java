package ru.sushi.delivery.kds;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import ru.sushi.delivery.kds.domain.BusinessLogic;
import ru.sushi.delivery.kds.domain.Item;
import ru.sushi.delivery.kds.service.ChefScreenService;

import java.util.ArrayList;
import java.util.List;

@Route("create")
public class CreateOrderView extends HorizontalLayout {

    private final List<Item> chosenItems = new ArrayList<>();
    private final ChefScreenService chefScreenService;


    public CreateOrderView(@Autowired ChefScreenService chefScreenService) {
        setSizeFull();

        this.chefScreenService = chefScreenService;

        List<Item> menuItems = BusinessLogic.items;

        // Левая часть (выбор блюд)
        VerticalLayout leftLayout = new VerticalLayout();
        leftLayout.setWidth("50%");
        leftLayout.setPadding(false);
        leftLayout.setSpacing(true);

        // Заголовок слева
        H3 leftTitle = new H3("Выберите блюдо:");
        ComboBox<Item> menuCombo = new ComboBox<>();
        menuCombo.setWidth("100%");
        menuCombo.setLabel("Блюдо из меню");
        menuCombo.setItems(menuItems);       // список из MenuHolder
        menuCombo.setItemLabelGenerator(Item::getName);  // отображаемое название

        // Кнопка "Добавить в заказ"
        Button addButton = new Button("Добавить в заказ");

        leftLayout.add(leftTitle, menuCombo, addButton);

        // Правая часть (список выбранных блюд)
        VerticalLayout rightLayout = new VerticalLayout();
        rightLayout.setWidth("50%");
        rightLayout.setPadding(false);
        rightLayout.setSpacing(true);

        H3 rightTitle = new H3("Список выбранных позиций:");
        // Таблица (Grid) для отображения выбранных блюд
        Grid<Item> chosenGrid = new Grid<>(Item.class, false);
        chosenGrid.addColumn(Item::getName).setHeader("Наименование");
        chosenGrid.setItems(chosenItems); // Привязываем к списку chosenItems

        // Кнопка "Создать заказ"
        Button createOrderButton = new Button("Создать заказ");

        rightLayout.add(rightTitle, chosenGrid, createOrderButton);

        // Добавляем оба лэйаута на главный горизонтальный
        add(leftLayout, rightLayout);

        // Обработка нажатия "Добавить в заказ"
        addButton.addClickListener(e -> {
            Item selected = menuCombo.getValue();
            if (selected == null) {
                Notification.show("Сначала выберите блюдо из меню");
                return;
            }
            // Добавляем выбранное блюдо в список
            chosenItems.add(selected);
            // Обновляем Grid
            chosenGrid.getDataProvider().refreshAll();

            // Очищаем ComboBox
            menuCombo.clear();
        });

        // Обработка нажатия "Создать заказ"
        createOrderButton.addClickListener(e -> {
                if (chosenItems.isEmpty()) {
                    Notification.show("Нельзя создать пустой заказ");
                    return;
                }

                for (var item : chosenItems) {
                    var place = item.getPlacesFlow().get(0);
                    item.setNextStep(item.getNextStep() + 1);
                    chefScreenService.addOrder(place.getDisplays().get(0), item);
                }

                // Здесь логика создания заказа в системе.
                // Например, можно вызвать ваш сервис: orderService.save(chosenItems);

                Notification.show("Заказ создан! Позиции: " + chosenItems.size());

                // Очистим список
                chosenItems.clear();
                chosenGrid.getDataProvider().refreshAll();
            }
        );
    }
}
