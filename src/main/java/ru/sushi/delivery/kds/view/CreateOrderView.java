package ru.sushi.delivery.kds.view;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import ru.sushi.delivery.kds.domain.persist.entity.ItemCombo;
import ru.sushi.delivery.kds.domain.persist.entity.product.MenuItem;
import ru.sushi.delivery.kds.dto.OrderFullDto;
import ru.sushi.delivery.kds.dto.OrderItemDto;
import ru.sushi.delivery.kds.model.OrderItemStationStatus;
import ru.sushi.delivery.kds.model.OrderStatus;
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

    private final List<MenuItem> chosenMenuItems = new ArrayList<>();
    private final ViewService viewService;
    private final CashListener cashListener;

    // Два контейнера под содержимое вкладок (Роллы / Сеты)
    private final VerticalLayout rollsTabLayout = new VerticalLayout();
    private final VerticalLayout setsTabLayout = new VerticalLayout();
    private final VerticalLayout extrasTabLayout = new VerticalLayout();

    // Grids слева
    private final Grid<MenuItem> rollsGrid = new Grid<>(MenuItem.class, false);
    private final Grid<ItemCombo> setsGrid = new Grid<>(ItemCombo.class, false);
    private final Grid<MenuItem> extrasGrid = new Grid<>(MenuItem.class, false);

    // Списки из BusinessLogic (исходный список)
    private final List<MenuItem> menuMenuItems;
    private final List<ItemCombo> menuItemCombos;
    private final List<MenuItem> menuExtras;

    // Таблица «Корзины» (справа, первая вкладка)
    private final Grid<MenuItem> chosenGrid = new Grid<>(MenuItem.class, false);

    // Таблица «Все заказы» (справа, вторая вкладка)
    private final Grid<OrderFullDto> ordersGrid = new Grid<>(OrderFullDto.class, false);

    // Поле для ввода «номера заказа»
    private final TextField orderNumberField = new TextField("Номер заказа");

    // Поле для отображения общей суммы к оплате
    private final H3 totalPay = new H3("К оплате: 0.0 рублей");

    @Autowired
    public CreateOrderView(ViewService viewService, CashListener cashListener) {
        setSizeFull();

        this.viewService = viewService;
        this.cashListener = cashListener;

        // Общие отступы и пространство между колонками
        getStyle().set("padding", "20px");
        getStyle().set("gap", "20px");

        // Загружаем списки из бизнес-логики
        this.menuMenuItems = viewService.getAllMenuItems(); // Роллы
        this.menuItemCombos = viewService.getAllCombos(); // Сеты
        this.menuExtras = viewService.getAllExtras();

        // ----------------------------
        // ЛЕВАЯ ЧАСТЬ
        // ----------------------------

        Tab tabRolls = new Tab("Роллы");
        Tab tabSets = new Tab("Сеты");
        Tab tabExtras = new Tab("Допы");
        Tabs tabsLeft = new Tabs(tabRolls, tabSets, tabExtras);
        tabsLeft.setWidthFull();

        Div tabsContentLeft = new Div(rollsTabLayout, setsTabLayout, extrasTabLayout);
        tabsContentLeft.setWidthFull();
        setsTabLayout.setVisible(false);
        extrasTabLayout.setVisible(false);

        tabsLeft.addSelectedChangeListener(event -> {
            if (event.getSelectedTab().equals(tabRolls)) {
                rollsTabLayout.setVisible(true);
                setsTabLayout.setVisible(false);
                extrasTabLayout.setVisible(false);
            }
            else if (event.getSelectedTab().equals(tabSets)) {
                rollsTabLayout.setVisible(false);
                setsTabLayout.setVisible(true);
                extrasTabLayout.setVisible(false);
            }
            else {
                rollsTabLayout.setVisible(false);
                setsTabLayout.setVisible(false);
                extrasTabLayout.setVisible(true);
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
                rollsGrid.setItems(menuMenuItems);
            }
            else {
                rollsGrid.setItems(
                        menuMenuItems.stream()
                                .filter(item -> item.getName().toLowerCase().contains(searchValue))
                                .collect(Collectors.toList())
                );
            }
        });

        rollsGrid.setItems(menuMenuItems);
        rollsGrid.addColumn(MenuItem::getName).setHeader("Наименование");
        rollsGrid.addColumn(MenuItem::getPrice).setHeader("Цена");
        rollsGrid.setWidthFull();
        rollsGrid.addItemClickListener(e -> {
            MenuItem clickedMenuItem = e.getItem();
            chosenMenuItems.add(clickedMenuItem);
            updateTotalPay();
            Notification.show(String.format(
                    "Добавлен: %s - %.1f рублей",
                    clickedMenuItem.getName(),
                    clickedMenuItem.getPrice()
            ));
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
                setsGrid.setItems(menuItemCombos);
            }
            else {
                setsGrid.setItems(
                        menuItemCombos.stream()
                                .filter(s -> s.getName().toLowerCase().contains(searchValue))
                                .collect(Collectors.toList())
                );
            }
        });

        setsGrid.setItems(menuItemCombos);
        setsGrid.addColumn(ItemCombo::getName).setHeader("Наименование");
        setsGrid.setWidthFull();
        setsGrid.addItemClickListener(e -> {
            ItemCombo clickedSet = e.getItem();
            chosenMenuItems.addAll(clickedSet.getMenuItems());
            updateTotalPay();
            Notification.show("Добавлен сет: " + clickedSet.getName());
            chosenGrid.getDataProvider().refreshAll();
        });

        setsTabLayout.add(setsSearchField, setsGrid);

        // --- Вкладка "Допы" --- (новая вкладка для допов)
        extrasTabLayout.setPadding(false);
        extrasTabLayout.setSpacing(true);

        TextField extrasSearchField = new TextField("Поиск по допам");
        extrasSearchField.setPlaceholder("Введите название...");
        extrasSearchField.setWidthFull();
        extrasSearchField.setValueChangeMode(ValueChangeMode.EAGER);
        extrasSearchField.addValueChangeListener(e -> {
            String searchValue = e.getValue().trim().toLowerCase();
            if (searchValue.isEmpty()) {
                extrasGrid.setItems(menuExtras);
            }
            else {
                extrasGrid.setItems(
                        menuExtras.stream()
                                .filter(item -> item.getName().toLowerCase().contains(searchValue))
                                .collect(Collectors.toList())
                );
            }
        });

        extrasGrid.setItems(menuExtras);
        extrasGrid.addColumn(MenuItem::getName).setHeader("Наименование");
        extrasGrid.addColumn(MenuItem::getPrice).setHeader("Цена");
        extrasGrid.setWidthFull();
        extrasGrid.addItemClickListener(e -> {
            MenuItem clickedMenuItem = e.getItem();
            chosenMenuItems.add(clickedMenuItem);
            updateTotalPay();
            Notification.show(String.format(
                    "Добавлен дополнительный товар: %s - %.1f рублей",
                    clickedMenuItem.getName(),
                    clickedMenuItem.getPrice()
            ));
            chosenGrid.getDataProvider().refreshAll();
        });

        extrasTabLayout.add(extrasSearchField, extrasGrid);

        VerticalLayout leftLayout = new VerticalLayout(
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
        orderNumberField.setPlaceholder("Введите номер заказа...");
        orderNumberField.setWidthFull();

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
            }
            else {
                cartLayout.setVisible(false);
                allOrdersLayout.setVisible(true);
                refreshOrdersGrid();
            }
        });

        VerticalLayout rightLayout = new VerticalLayout(rightTabs, orderNumberField, cartLayout, allOrdersLayout);
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
        chosenGrid.addColumn(MenuItem::getName).setHeader("Наименование");
        chosenGrid.addColumn(MenuItem::getPrice).setHeader("Цена");
        chosenGrid.setItems(chosenMenuItems);

        Button createOrderButton = new Button("Создать заказ");
        Button clearCartButton = new Button("Очистить корзину");
        HorizontalLayout buttonBar = new HorizontalLayout(createOrderButton, clearCartButton);

        createOrderButton.addClickListener(e -> {
            if (chosenMenuItems.isEmpty()) {
                Notification.show("Корзина пуста, нельзя создать заказ");
                return;
            }

            String orderNumber = orderNumberField.getValue().trim();
            if (orderNumber.isEmpty()) {
                Notification.show("Нельзя создать заказ без номера");
                return;
            }

            viewService.createOrder(orderNumber, chosenMenuItems);
            Notification.show("Заказ создан! Номер: " + orderNumber +
                    ", Позиции: " + chosenMenuItems.size());

            chosenMenuItems.clear();
            updateTotalPay();
            chosenGrid.getDataProvider().refreshAll();
            orderNumberField.clear();
        });

        clearCartButton.addClickListener(e -> {
            chosenMenuItems.clear();
            updateTotalPay();
            chosenGrid.getDataProvider().refreshAll();
            Notification.show("Корзина очищена");
        });

        cartLayout.add(chosenTitle, chosenGrid, totalPay, buttonBar);
        return cartLayout;
    }

    private void updateTotalPay() {
        double total = chosenMenuItems.stream().mapToDouble(MenuItem::getPrice).sum();
        totalPay.setText("К оплате: " + total + " рублей");
    }

    /**
     * Создаем лейаут "Все заказы" (Grid со всеми заказами).
     */
    private Div buildAllOrdersLayout() {
        Div ordersLayout = new Div();
        ordersLayout.setWidthFull();

        ordersGrid.removeAllColumns();

        ordersGrid.addColumn(OrderFullDto::getName)
                .setHeader("ID");

        ordersGrid.addColumn(dto -> {
            if (dto.getItems() == null) {
                return 0;
            }
            return dto.getItems().stream()
                    .filter(item -> item.getStatus() != OrderItemStationStatus.CANCELED)
                    .count();
        }).setHeader("Кол-во позиций");

        ordersGrid.addColumn(orderDto -> switch (orderDto.getStatus()) {
            case CREATED -> "Создан";
            case COOKING -> "Готовится";
            case COLLECTING -> "Сборка";
            case READY -> "Выполнен";
            case CANCELED -> "Отменён";
            default -> "";
        }).setHeader("Статус");

        ordersGrid.addComponentColumn(orderDto -> {
            HorizontalLayout layout = new HorizontalLayout();

            Button detailsBtn = new Button("Позиции");
            detailsBtn.addClickListener(e -> openOrderItemsDialog(orderDto));
            layout.add(detailsBtn);

            if (orderDto.getStatus() != OrderStatus.READY && orderDto.getStatus() != OrderStatus.CANCELED) {
                Button cancelBtn = new Button("Отменить");
                cancelBtn.addClickListener(e -> {
                    viewService.cancelOrder(orderDto.getId());
                    Notification.show("Заказ " + orderDto.getName() + " отменён!");
                    refreshOrdersGrid();
                });
                layout.add(cancelBtn);
            }
            return layout;
        }).setHeader("Действие");

        ordersLayout.add(new H3("Список всех заказов:"), ordersGrid);
        return ordersLayout;
    }

    /**
     * Обновляем таблицу «Все заказы».
     */
    private void refreshOrdersGrid() {
        List<OrderFullDto> allOrders = viewService.getAllOrdersWithItems();

        List<OrderFullDto> notCanceled = allOrders.stream()
                .filter(dto -> !OrderStatus.CANCELED.equals(dto.getStatus()))
                .collect(Collectors.toList());

        ordersGrid.setItems(notCanceled);
    }

    /**
     * Открываем диалог с позициями выбранного заказа.
     */
    private void openOrderItemsDialog(OrderFullDto orderDto) {
        Dialog dialog = new Dialog();
        dialog.setWidth("600px");
        dialog.setHeaderTitle("Позиции заказа: " + orderDto.getName());

        Grid<OrderItemDto> itemsGrid = new Grid<>(OrderItemDto.class, false);
        itemsGrid.addColumn(OrderItemDto::getName).setHeader("Наименование");

        if (!OrderStatus.READY.name().equalsIgnoreCase(orderDto.getStatus().toString())) {

            itemsGrid.addComponentColumn(itemDto -> {
                if ("CANCELED".equals(itemDto.getStatus().name())) {
                    return new Span("Отменено");
                }

                Button removeBtn = new Button("Удалить");
                removeBtn.addClickListener(click -> {
                    viewService.cancelOrderItem(itemDto.getId());
                    Notification.show("Позиция удалена (ID=" + itemDto.getId() + ")");
                    itemsGrid.setItems(this.viewService.getOrderItems(orderDto.getId()));
                });

                return removeBtn;
            }).setHeader("Действие");

            Button addItemBtn = new Button("Добавить позицию");
            addItemBtn.addClickListener(ev -> {
                Dialog addDialog = buildAddItemsDialog(orderDto.getId(), itemsGrid);
                addDialog.open();
            });
            dialog.getFooter().add(addItemBtn);
        }

        itemsGrid.setItems(orderDto.getItems());

        VerticalLayout layout = new VerticalLayout(itemsGrid);
        dialog.add(layout);

        Button closeBtn = new Button("Закрыть", event -> dialog.close());
        dialog.getFooter().add(closeBtn);

        dialog.open();
    }

    /**
     * Строим диалог «Добавить позицию» — показываем список доступных роллов (menuItems),
     * при клике добавляем в заказ, обновляем грид в родительском диалоге.
     *
     * @param orderId строковый «ID заказа»
     * @param itemsGrid грид, который нужно обновить после добавления
     *
     * @return Dialog
     */
    private Dialog buildAddItemsDialog(Long orderId, Grid<OrderItemDto> itemsGrid) {
        Dialog addDialog = new Dialog();
        addDialog.setWidth("600px");
        addDialog.setHeaderTitle("Выберите позицию для добавления");

        Tab rollsTab = new Tab("Роллы");
        Tab setsTab = new Tab("Сеты");
        Tabs tabs = new Tabs(rollsTab, setsTab);

        VerticalLayout rollsLayout = new VerticalLayout();
        VerticalLayout setsLayout = new VerticalLayout();

        setsLayout.setVisible(false);
        tabs.addSelectedChangeListener(e -> {
            if (e.getSelectedTab() == rollsTab) {
                rollsLayout.setVisible(true);
                setsLayout.setVisible(false);
            }
            else {
                rollsLayout.setVisible(false);
                setsLayout.setVisible(true);
            }
        });

        TextField rollSearchField = new TextField("Поиск по роллам");
        rollSearchField.setPlaceholder("Введите название...");
        rollSearchField.setValueChangeMode(ValueChangeMode.EAGER);

        Grid<MenuItem> rollsGrid = new Grid<>(MenuItem.class, false);
        rollsGrid.setWidthFull();
        rollsGrid.addColumn(MenuItem::getName).setHeader("Наименование");

        rollsGrid.addComponentColumn(item -> {
            Button addButton = new Button("Добавить");
            addButton.addClickListener(click -> {
                viewService.addItemToOrder(orderId, item);
                Notification.show("Добавлено: " + item.getName());

                itemsGrid.setItems(this.viewService.getOrderItems(orderId));
            });
            return addButton;
        }).setHeader("Действие");

        rollsGrid.setItems(menuMenuItems);

        rollSearchField.addValueChangeListener(ev -> {
            String search = ev.getValue().toLowerCase().trim();
            if (search.isEmpty()) {
                rollsGrid.setItems(menuMenuItems);
            }
            else {
                rollsGrid.setItems(
                        menuMenuItems.stream()
                                .filter(item -> item.getName().toLowerCase().contains(search))
                                .collect(Collectors.toList())
                );
            }
        });

        rollsLayout.add(rollSearchField, rollsGrid);

        TextField setSearchField = new TextField("Поиск по сетам");
        setSearchField.setPlaceholder("Введите название...");
        setSearchField.setValueChangeMode(ValueChangeMode.EAGER);

        Grid<ItemCombo> setsGrid = new Grid<>(ItemCombo.class, false);
        setsGrid.setWidthFull();
        setsGrid.addColumn(ItemCombo::getName).setHeader("Наименование");

        // Добавляем колонку с кнопкой «Добавить»
        setsGrid.addComponentColumn(set -> {
            Button addButton = new Button("Добавить");
            addButton.addClickListener(click -> {
                for (MenuItem i : set.getMenuItems()) {
                    viewService.addItemToOrder(orderId, i);
                }
                Notification.show("Добавлен сет: " + set.getName());

                // Обновляем основной грид позиций
                itemsGrid.setItems(this.viewService.getOrderItems(orderId));
            });
            return addButton;
        }).setHeader("Действие");

        setsGrid.setItems(menuItemCombos);

        // Фильтрация при вводе
        setSearchField.addValueChangeListener(ev -> {
            String search = ev.getValue().toLowerCase().trim();
            if (search.isEmpty()) {
                setsGrid.setItems(menuItemCombos);
            }
            else {
                setsGrid.setItems(
                        menuItemCombos.stream()
                                .filter(set -> set.getName().toLowerCase().contains(search))
                                .collect(Collectors.toList())
                );
            }
        });

        setsLayout.add(setSearchField, setsGrid);

        // ---------------------------
        // Объединяем всё в контент
        // ---------------------------
        Div tabsContent = new Div(rollsLayout, setsLayout);
        tabsContent.setSizeFull();

        VerticalLayout content = new VerticalLayout(tabs, tabsContent);
        content.setPadding(false);
        content.setSpacing(true);
        content.setSizeFull();

        addDialog.add(content);

        // Footer (кнопка «Отмена»)
        Button cancelBtn = new Button("Отмена", ev -> addDialog.close());
        addDialog.getFooter().add(cancelBtn);

        return addDialog;
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
