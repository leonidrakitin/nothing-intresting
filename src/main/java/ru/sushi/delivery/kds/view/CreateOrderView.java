package ru.sushi.delivery.kds.view;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import ru.sushi.delivery.kds.domain.persist.entity.Item;
import ru.sushi.delivery.kds.domain.persist.entity.ItemSet;
import ru.sushi.delivery.kds.dto.OrderFullDto;
import ru.sushi.delivery.kds.service.ViewService;
import ru.sushi.delivery.kds.service.dto.BroadcastMessage;
import ru.sushi.delivery.kds.service.dto.BroadcastMessageType;
import ru.sushi.delivery.kds.service.listeners.BroadcastListener;
import ru.sushi.delivery.kds.service.listeners.CashListener;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Route("create")
public class CreateOrderView extends HorizontalLayout implements BroadcastListener {

    private final List<Item> chosenItems = new ArrayList<>();
    private final ViewService viewService;
    private final CashListener cashListener;

    // Два контейнера под содержимое вкладок (Роллы / Сеты)
    private final VerticalLayout rollsTabLayout = new VerticalLayout();
    private final VerticalLayout setsTabLayout = new VerticalLayout();

    // Grids слева
    private final Grid<Item> rollsGrid = new Grid<>(Item.class, false);
    private final Grid<ItemSet> setsGrid = new Grid<>(ItemSet.class, false);

    // Списки из BusinessLogic (исходный список)
    private final List<Item> menuItems;
    private final List<ItemSet> menuItemSets;

    // Таблица «Корзины» справа
    private final Grid<Item> chosenGrid = new Grid<>(Item.class, false);

    // Таблица «Все заказы» (справа, отдельная вкладка)
    private final Grid<OrderFullDto> ordersGrid = new Grid<>(OrderFullDto.class, false);

    // Поле для ввода «номера заказа»
    private final TextField orderNumberField = new TextField("Номер заказа");

    @Autowired
    public CreateOrderView(ViewService viewService, CashListener cashListener) {
        setSizeFull();

        this.viewService = viewService;
        this.cashListener = cashListener;

        // Общие отступы и пространство между колонками
        getStyle().set("padding", "20px");
        getStyle().set("gap", "20px");

        // Загружаем списки из BusinessLogic
        this.menuItems = viewService.getAllMenuItems(); // Роллы
        this.menuItemSets = List.of();                  // Сеты (пример)

        // ----------------------------
        // ЛЕВАЯ ЧАСТЬ
        // ----------------------------

        // Поле «Номер заказа»
        orderNumberField.setPlaceholder("Введите номер заказа...");
        orderNumberField.setWidthFull();

        // Создаём две вкладки (Роллы, Сеты)
        Tab tabRolls = new Tab("Роллы");
        Tab tabSets = new Tab("Сеты");
        Tabs tabsLeft = new Tabs(tabRolls, tabSets);
        tabsLeft.setWidthFull();

        // Два Layout’а для контента (rollsTabLayout, setsTabLayout)
        Div tabsContentLeft = new Div(rollsTabLayout, setsTabLayout);
        tabsContentLeft.setWidthFull();
        // Изначально показываем «Роллы»
        setsTabLayout.setVisible(false);

        tabsLeft.addSelectedChangeListener(event -> {
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

        TextField rollsSearchField = new TextField("Поиск по роллам");
        rollsSearchField.setPlaceholder("Введите название...");
        rollsSearchField.setWidthFull();
        rollsSearchField.setValueChangeMode(ValueChangeMode.EAGER);
        rollsSearchField.addValueChangeListener(e -> {
            String searchValue = e.getValue().trim().toLowerCase();
            if (searchValue.isEmpty()) {
                rollsGrid.setItems(menuItems);
            } else {
                rollsGrid.setItems(
                    menuItems.stream()
                        .filter(item -> item.getName().toLowerCase().contains(searchValue))
                        .collect(Collectors.toList())
                );
            }
        });

        rollsGrid.setItems(menuItems);
        rollsGrid.addColumn(Item::getName).setHeader("Наименование");
        rollsGrid.setWidthFull();
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

        TextField setsSearchField = new TextField("Поиск по сетам");
        setsSearchField.setPlaceholder("Введите название...");
        setsSearchField.setWidthFull();
        setsSearchField.setValueChangeMode(ValueChangeMode.EAGER);
        setsSearchField.addValueChangeListener(e -> {
            String searchValue = e.getValue().trim().toLowerCase();
            if (searchValue.isEmpty()) {
                setsGrid.setItems(menuItemSets);
            } else {
                setsGrid.setItems(
                    menuItemSets.stream()
                        .filter(s -> s.getName().toLowerCase().contains(searchValue))
                        .collect(Collectors.toList())
                );
            }
        });

        setsGrid.setItems(menuItemSets);
        setsGrid.addColumn(ItemSet::getName).setHeader("Наименование");
        setsGrid.setWidthFull();
        setsGrid.addItemClickListener(e -> {
            ItemSet clickedSet = e.getItem();
            chosenItems.addAll(clickedSet.getItems());
            Notification.show("Добавлен сет: " + clickedSet.getName());
            chosenGrid.getDataProvider().refreshAll();
        });

        setsTabLayout.add(setsSearchField, setsGrid);

        // Собираем левую часть (вертикально): [Поле "Номер заказа"] + [Tabs] + [Контент вкладок]
        VerticalLayout leftLayout = new VerticalLayout(
            orderNumberField,
            tabsLeft,
            tabsContentLeft
        );
        leftLayout.setWidth("50%");
        leftLayout.setPadding(false);
        leftLayout.setSpacing(true);
        leftLayout.getStyle()
            .set("border", "1px solid #ccc")
            .set("border-radius", "8px")
            .set("padding", "20px");

        // ----------------------------
        // ПРАВАЯ ЧАСТЬ (Корзина / Все заказы)
        // ----------------------------
        Tab cartTab = new Tab("Корзина");
        Tab ordersTab = new Tab("Все заказы");
        Tabs rightTabs = new Tabs(cartTab, ordersTab);

        Div cartLayout = buildCartLayout();
        Div allOrdersLayout = buildAllOrdersLayout();
        allOrdersLayout.setVisible(false);

        rightTabs.addSelectedChangeListener(event -> {
            if (event.getSelectedTab().equals(cartTab)) {
                cartLayout.setVisible(true);
                allOrdersLayout.setVisible(false);
            } else {
                cartLayout.setVisible(false);
                allOrdersLayout.setVisible(true);
                refreshOrdersGrid();
            }
        });

        VerticalLayout rightLayout = new VerticalLayout(rightTabs, cartLayout, allOrdersLayout);
        rightLayout.setWidth("50%");
        rightLayout.setPadding(false);
        rightLayout.setSpacing(true);
        rightLayout.getStyle()
            .set("border", "1px solid #ccc")
            .set("border-radius", "8px")
            .set("padding", "20px");

        // Добавляем левую и правую часть на главный лейаут
        add(leftLayout, rightLayout);
    }

    /**
     * Создаем лейаут "Корзина".
     */
    private Div buildCartLayout() {
        Div cartLayout = new Div();
        cartLayout.setWidthFull();

        H3 chosenTitle = new H3("Корзина:");
        chosenGrid.addColumn(Item::getName).setHeader("Наименование");
        chosenGrid.setItems(chosenItems);

        Button createOrderButton = new Button("Создать заказ");
        Button clearCartButton = new Button("Очистить корзину");
        HorizontalLayout buttonBar = new HorizontalLayout(createOrderButton, clearCartButton);

        createOrderButton.addClickListener(e -> {
            if (chosenItems.isEmpty()) {
                Notification.show("Корзина пуста, нельзя создать заказ");
                return;
            }
            // Берём значение "номера заказа" из поля orderNumberField
            String orderNumber = orderNumberField.getValue().trim();
            if (orderNumber.isEmpty()) {
                orderNumber = "Без номера"; // или любой дефолт
            }

            // Создаём заказ с указанным номером
            viewService.createOrder(orderNumber, chosenItems);
            Notification.show("Заказ создан! Номер: " + orderNumber +
                ", Позиции: " + chosenItems.size());

            chosenItems.clear();
            chosenGrid.getDataProvider().refreshAll();
        });

        clearCartButton.addClickListener(e -> {
            chosenItems.clear();
            chosenGrid.getDataProvider().refreshAll();
            Notification.show("Корзина очищена");
        });

        cartLayout.add(chosenTitle, chosenGrid, buttonBar);
        return cartLayout;
    }

    /**
     * Создаем лейаут "Все заказы" (Grid со всеми заказами).
     */
    private Div buildAllOrdersLayout() {
        Div ordersLayout = new Div();
        ordersLayout.setWidthFull();

        ordersGrid.removeAllColumns();
        ordersGrid.addColumn(OrderFullDto::getOrderId).setHeader("ID");
        ordersGrid.addColumn(dto -> dto.getItems() == null ? 0 : dto.getItems().size())
            .setHeader("Кол-во позиций");

        // Пример вывода статуса в человеко-понятном виде
        ordersGrid.addColumn(orderDto -> {
            return switch (orderDto.getStatus()) {
                case "CREATED" -> "Создан";
                case "COOKING" -> "Готовится";
                case "COLLECTING" -> "Сборка заказа";
                case "READY" -> "Выполнен";
                default -> "";
            };
        }).setHeader("Статус заказа");

        ordersLayout.add(new H3("Список всех заказов:"), ordersGrid);
        return ordersLayout;
    }

    /**
     * Обновляем таблицу «Все заказы».
     */
    private void refreshOrdersGrid() {
        List<OrderFullDto> allOrders = viewService.getAllOrdersWithItems();
        ordersGrid.setItems(allOrders);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        this.cashListener.register(this);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        this.cashListener.unregister(this);
        super.onDetach(detachEvent);
    }

    @Override
    public void receiveBroadcast(BroadcastMessage message) {
        if (message.getType() == BroadcastMessageType.NOTIFICATION) {
            Notification.show(message.getContent());
        }
    }
}
