package ru.sushi.delivery.kds.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import ru.sushi.delivery.kds.domain.persist.entity.Item;
import ru.sushi.delivery.kds.domain.persist.entity.ItemSet;
import ru.sushi.delivery.kds.domain.util.BusinessLogic;
import ru.sushi.delivery.kds.service.ChefScreenOrderChangesListener;
import ru.sushi.delivery.kds.service.ViewService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Route("create")
public class CreateOrderView extends HorizontalLayout {

    private final List<Item> chosenItems = new ArrayList<>();
    private final ViewService viewService;

    // Два контейнера под содержимое вкладок
    private final VerticalLayout rollsTabLayout = new VerticalLayout();
    private final VerticalLayout setsTabLayout = new VerticalLayout();

    // Grids
    private final Grid<Item> rollsGrid = new Grid<>(Item.class, false);
    private final Grid<ItemSet> setsGrid = new Grid<>(ItemSet.class, false);

    // Списки из BusinessLogic (исходный список)
    private final List<Item> menuItems;
    private final List<ItemSet> menuItemSets;

    // Таблица корзины справа
    private final Grid<Item> chosenGrid = new Grid<>(Item.class, false);

    @Autowired
    public CreateOrderView(ViewService viewService) {
        setSizeFull();

        this.viewService = viewService;

        // Общие отступы и пространство между колонками
        getStyle().set("padding", "20px");
        getStyle().set("gap", "20px");

        // Изначально загружаем списки из BusinessLogic
        this.menuItems = BusinessLogic.items;           // Роллы
        this.menuItemSets = BusinessLogic.itemSets;     // Сеты

        // -----------------------------------------------------------
        // Левая часть: вкладки (Tabs) со списками роллов и сетов
        // -----------------------------------------------------------

        // Создаём две вкладки
        Tab tabRolls = new Tab("Роллы");
        Tab tabSets = new Tab("Сеты");
        Tabs tabs = new Tabs(tabRolls, tabSets);
        tabs.setWidthFull();

        // Контейнер, где размещаем layouts для роллов и сетов
        Div tabsContent = new Div(rollsTabLayout, setsTabLayout);
        tabsContent.setWidthFull();
        // Изначально показываем «Роллы», «Сеты» скрываем
        setsTabLayout.setVisible(false);

        // Логика переключения вкладок
        tabs.addSelectedChangeListener(event -> {
            if (event.getSelectedTab().equals(tabRolls)) {
                rollsTabLayout.setVisible(true);
                setsTabLayout.setVisible(false);
            } else {
                rollsTabLayout.setVisible(false);
                setsTabLayout.setVisible(true);
            }
        });

        // --- Вкладка "Роллы" ---
        rollsTabLayout.setPadding(false);
        rollsTabLayout.setSpacing(true);

        // Поле поиска по роллам
        TextField rollsSearchField = new TextField("Поиск по роллам");
        rollsSearchField.setPlaceholder("Введите название...");
        rollsSearchField.setWidthFull();
        rollsSearchField.setValueChangeMode(ValueChangeMode.EAGER);


        // При вводе текста в поле фильтруем список
        rollsSearchField.addValueChangeListener(e -> {
            String searchValue = e.getValue().trim().toLowerCase();
            if (searchValue.isEmpty()) {
                rollsGrid.setItems(menuItems);
            } else {
                rollsGrid.setItems(menuItems.stream()
                    .filter(item -> item.getName().toLowerCase().contains(searchValue))
                    .collect(Collectors.toList()));
            }
        });

        // Grid с роллами
        rollsGrid.setItems(menuItems);               // Начально показываем все
        rollsGrid.addColumn(Item::getName);
        rollsGrid.setWidthFull();

        // Клик по строке => Добавить выбранный Item в корзину
        rollsGrid.addItemClickListener(e -> {
            Item clickedItem = e.getItem();
            chosenItems.add(clickedItem);
            Notification.show("Добавлен: " + clickedItem.getName());
            chosenGrid.getDataProvider().refreshAll();
        });

        rollsTabLayout.add(rollsSearchField, rollsGrid);

        // --- Вкладка "Сеты" ---
        setsTabLayout.setPadding(false);
        setsTabLayout.setSpacing(true);

        // Поле поиска по сетам
        TextField setsSearchField = new TextField("Поиск по сетам");
        setsSearchField.setPlaceholder("Введите название...");
        setsSearchField.setWidthFull();
        rollsSearchField.setValueChangeMode(ValueChangeMode.EAGER);


        setsSearchField.addValueChangeListener(e -> {
            String searchValue = e.getValue().trim().toLowerCase();
            if (searchValue.isEmpty()) {
                setsGrid.setItems(menuItemSets);
            } else {
                setsGrid.setItems(menuItemSets.stream()
                    .filter(s -> s.getName().toLowerCase().contains(searchValue))
                    .collect(Collectors.toList()));
            }
        });

        // Grid с сетами
        setsGrid.setItems(menuItemSets);
        setsGrid.addColumn(ItemSet::getName);
        setsGrid.setWidthFull();

        // Клик по строке => Добавить все Item из этого сета
        setsGrid.addItemClickListener(e -> {
            ItemSet clickedSet = e.getItem();
            chosenItems.addAll(clickedSet.getItems());
            Notification.show("Добавлен сет: " + clickedSet.getName());
            chosenGrid.getDataProvider().refreshAll();
        });

        setsTabLayout.add(setsSearchField, setsGrid);

        // Объединяем Tabs и «tabsContent» во VerticalLayout
        VerticalLayout leftLayout = new VerticalLayout(tabs, tabsContent);
        leftLayout.setWidth("50%");
        leftLayout.setPadding(false);
        leftLayout.setSpacing(true);
        leftLayout.getStyle()
            .set("border", "1px solid #ccc")
            .set("border-radius", "8px")
            .set("padding", "20px");

        // -----------------------------------------------------------
        // Правая часть: корзина (список выбранных позиций) + кнопки
        // -----------------------------------------------------------

        VerticalLayout rightLayout = new VerticalLayout();
        rightLayout.setWidth("50%");
        rightLayout.setSpacing(true);
        rightLayout.getStyle()
            .set("border", "1px solid #ccc")
            .set("border-radius", "8px")
            .set("padding", "20px");

        H3 chosenTitle = new H3("Корзина:");
        chosenGrid.addColumn(Item::getName).setHeader("Наименование");
        chosenGrid.setItems(chosenItems);

        // Кнопки "Создать заказ" и "Очистить корзину"
        Button createOrderButton = new Button("Создать заказ");
        Button clearCartButton = new Button("Очистить корзину");
        HorizontalLayout buttonBar = new HorizontalLayout(createOrderButton, clearCartButton);

        rightLayout.add(chosenTitle, chosenGrid, buttonBar);

        // Добавляем левую и правую часть на главный лейаут
        add(leftLayout, rightLayout);

        // --- ЛОГИКА КНОПОК ---

        // "Создать заказ"
        createOrderButton.addClickListener(e -> {
            if (chosenItems.isEmpty()) {
                Notification.show("Корзина пуста, нельзя создать заказ");
                return;
            }
            //todo name заполнить
            viewService.createOrder("#123123", chosenItems);

            ChefScreenOrderChangesListener.broadcast("Новый заказ");
            Notification.show("Заказ создан! Позиции: " + chosenItems.size());

            chosenItems.clear();
            chosenGrid.getDataProvider().refreshAll();
        });

        // "Очистить корзину"
        clearCartButton.addClickListener(e -> {
            chosenItems.clear();
            chosenGrid.getDataProvider().refreshAll();
            Notification.show("Корзина очищена");
        });
    }
}
