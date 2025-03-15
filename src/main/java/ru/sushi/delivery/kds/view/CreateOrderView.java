package ru.sushi.delivery.kds.view;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
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
import ru.sushi.delivery.kds.dto.OrderItemDto;
import ru.sushi.delivery.kds.dto.OrderShortDto;
import ru.sushi.delivery.kds.model.OrderItemStationStatus;
import ru.sushi.delivery.kds.model.OrderStatus;
import ru.sushi.delivery.kds.service.ViewService;
import ru.sushi.delivery.kds.service.dto.BroadcastMessage;
import ru.sushi.delivery.kds.service.dto.BroadcastMessageType;
import ru.sushi.delivery.kds.service.listeners.BroadcastListener;
import ru.sushi.delivery.kds.service.listeners.CashListener;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

class CartItem {
    private final MenuItem menuItem;
    private int quantity;

    public CartItem(MenuItem menuItem, int quantity) {
        this.menuItem = menuItem;
        this.quantity = quantity;
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void increment() {
        this.quantity++;
    }

    public void decrement() {
        if (this.quantity > 0) {
            this.quantity--;
        }
    }
}

@Route("create")
public class CreateOrderView extends HorizontalLayout implements BroadcastListener {

    private final List<CartItem> cartItems = new ArrayList<>();
    private final ViewService viewService;
    private final CashListener cashListener;
    private Instant selectedKitchenStart = null;
    private final Span kitchenStartDisplay = new Span("Время начала приготовления: Сейчас");
    private final Button changeKitchenStartButton = new Button("изменить");
    private final DateTimePicker finishPicker = new DateTimePicker("Время готовности");
    private final Checkbox isYandexOrder = new Checkbox("Заказ Яндекс"); // Новый чекбокс
    private final VerticalLayout rollsTabLayout = new VerticalLayout();
    private final VerticalLayout setsTabLayout = new VerticalLayout();
    private final VerticalLayout extrasLayout = new VerticalLayout();

    private final Grid<MenuItem> rollsGrid = new Grid<>(MenuItem.class, false);
    private final Grid<ItemCombo> setsGrid = new Grid<>(ItemCombo.class, false);
    private final List<MenuItem> menuMenuItems;
    private final List<ItemCombo> menuItemCombos;
    private final List<MenuItem> menuExtras;

    private final Grid<CartItem> chosenGrid = new Grid<>(CartItem.class, false);
    private final Grid<OrderShortDto> ordersGrid = new Grid<>(OrderShortDto.class, false);
    private final TextField orderNumberField = new TextField("Номер заказа");
    private final H3 totalPay = new H3("К оплате: 0.0 рублей");
    private final H5 totalTime = new H5("Общее время приготовления: 0 минут");

    @Autowired
    public CreateOrderView(ViewService viewService, CashListener cashListener) {
        setSizeFull();

        this.viewService = viewService;
        this.cashListener = cashListener;

        getStyle().set("padding", "20px");
        getStyle().set("gap", "20px");

        this.menuMenuItems = viewService.getAllMenuItems();
        this.menuItemCombos = viewService.getAllCombos();
        this.menuExtras = viewService.getAllExtras();

        finishPicker.setLocale(Locale.of("ru", "RU"));

        // ЛЕВАЯ ЧАСТЬ
        Tab tabRolls = new Tab("Роллы");
        Tab tabSets = new Tab("Сеты");
        Tabs tabsLeft = new Tabs(tabRolls, tabSets);
        tabsLeft.setWidthFull();

        Div tabsContentLeft = new Div(rollsTabLayout, setsTabLayout);
        tabsContentLeft.setWidthFull();
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

        // Вкладка "Роллы"
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
            } else {
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
        rollsGrid.addItemClickListener(e -> addToCart(e.getItem()));

        rollsTabLayout.add(rollsSearchField, rollsGrid);

        // Вкладка "Сеты"
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
            } else {
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
            for (MenuItem item : clickedSet.getMenuItems()) {
                addToCart(item);
            }
            Notification.show("Добавлен сет: " + clickedSet.getName());
        });

        setsTabLayout.add(setsSearchField, setsGrid);

        // Допы (две колонки)
        extrasLayout.setPadding(false);
        extrasLayout.setSpacing(true);

        VerticalLayout extrasListLeft = new VerticalLayout();
        extrasListLeft.setPadding(false);
        extrasListLeft.setSpacing(false);

        VerticalLayout extrasListRight = new VerticalLayout();
        extrasListRight.setPadding(false);
        extrasListRight.setSpacing(false);

        HorizontalLayout extrasColumns = new HorizontalLayout(extrasListLeft, extrasListRight);
        extrasColumns.setWidthFull();
        extrasColumns.setSpacing(true);
        updateExtrasList(extrasListLeft, extrasListRight, menuExtras); // Инициализируем список допов

        extrasLayout.add(new H4("Допы"), extrasColumns);

        VerticalLayout leftLayout = new VerticalLayout(tabsLeft, tabsContentLeft, extrasLayout);
        leftLayout.setWidth("50%");
        leftLayout.setPadding(false);
        leftLayout.setSpacing(true);
        leftLayout.getStyle()
                .set("border", "1px solid #ccc")
                .set("border-radius", "8px")
                .set("padding", "20px");

        // ПРАВАЯ ЧАСТЬ
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
            } else {
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

        add(leftLayout, rightLayout);
    }

    // Метод для обновления списка допов в две колонки
    private void updateExtrasList(VerticalLayout leftColumn, VerticalLayout rightColumn, List<MenuItem> items) {
        leftColumn.removeAll();
        rightColumn.removeAll();

        int halfSize = (items.size() + 1) / 2; // Делим список на две части, округляя вверх
        for (int i = 0; i < items.size(); i++) {
            MenuItem item = items.get(i);
            HorizontalLayout itemLayout = new HorizontalLayout();
            itemLayout.setWidthFull();
            itemLayout.setAlignItems(Alignment.CENTER);

            Span name = new Span(item.getName());
            Span price = new Span(String.format("%.1f руб.", item.getPrice()));
            Button addButton = new Button(VaadinIcon.PLUS.create());
            Button removeButton = new Button(VaadinIcon.MINUS.create());
            Span quantity = new Span(String.valueOf(getCartQuantity(item)));

            addButton.addClickListener(e -> addToCart(item));
            removeButton.addClickListener(e -> removeFromCart(item, quantity));

            itemLayout.add(name, price, removeButton, quantity, addButton);
            itemLayout.setFlexGrow(1, name);

            if (i < halfSize) {
                leftColumn.add(itemLayout);
            } else {
                rightColumn.add(itemLayout);
            }
        }
    }

    private void addToCart(MenuItem item) {
        CartItem existingItem = cartItems.stream()
                .filter(cartItem -> cartItem.getMenuItem().equals(item))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.increment();
        } else {
            cartItems.add(new CartItem(item, 1));
        }

        updateTotalPay();
        updateTotalTime();
        Notification.show(String.format("Добавлен: %s - %.1f рублей", item.getName(), item.getPrice()));
        chosenGrid.getDataProvider().refreshAll();
        updateExtrasList(
                (VerticalLayout) ((HorizontalLayout) extrasLayout.getComponentAt(1)).getComponentAt(0),
                (VerticalLayout) ((HorizontalLayout) extrasLayout.getComponentAt(1)).getComponentAt(1),
                menuExtras
        ); // Обновляем список допов
    }

    private void removeFromCart(MenuItem item, Span quantityLabel) {
        CartItem existingItem = cartItems.stream()
                .filter(cartItem -> cartItem.getMenuItem().equals(item))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            if (existingItem.getQuantity() > 1) {
                existingItem.decrement();
            } else {
                cartItems.remove(existingItem);
            }
            updateTotalPay();
            updateTotalTime();
            chosenGrid.getDataProvider().refreshAll();
            quantityLabel.setText(String.valueOf(getCartQuantity(item))); // Обновляем количество в списке
            Notification.show(String.format("Удален: %s", item.getName()));
        }
    }

    private int getCartQuantity(MenuItem item) {
        return cartItems.stream()
                .filter(cartItem -> cartItem.getMenuItem().equals(item))
                .mapToInt(CartItem::getQuantity)
                .findFirst()
                .orElse(0);
    }

    private Div buildCartLayout() {
        Div cartLayout = new Div();
        cartLayout.setWidthFull();

        H3 chosenTitle = new H3("Корзина:");
        chosenGrid.addColumn(cartItem -> cartItem.getMenuItem().getName()).setHeader("Наименование");
        chosenGrid.addColumn(CartItem::getQuantity).setHeader("Кол-во");
        chosenGrid.addColumn(cartItem -> cartItem.getMenuItem().getPrice() * cartItem.getQuantity()).setHeader("Цена");
        chosenGrid.addComponentColumn(cartItem -> {
            HorizontalLayout buttons = new HorizontalLayout();
            Button removeBtn = new Button(VaadinIcon.MINUS.create());
            removeBtn.addClickListener(e -> {
                if (cartItem.getQuantity() > 1) {
                    cartItem.decrement();
                } else {
                    cartItems.remove(cartItem);
                }
                updateTotalPay();
                updateTotalTime();
                chosenGrid.getDataProvider().refreshAll();
                updateExtrasList(
                        (VerticalLayout) ((HorizontalLayout) extrasLayout.getComponentAt(1)).getComponentAt(0),
                        (VerticalLayout) ((HorizontalLayout) extrasLayout.getComponentAt(1)).getComponentAt(1),
                        menuExtras
                );
            });
            buttons.add(removeBtn);
            return buttons;
        }).setHeader("Действие");
        chosenGrid.setItems(cartItems);

        HorizontalLayout kitchenStartLayout = new HorizontalLayout(kitchenStartDisplay, changeKitchenStartButton);
        kitchenStartLayout.setAlignItems(Alignment.CENTER);
        changeKitchenStartButton.addClickListener(e -> openKitchenStartDialog());

        finishPicker.setValue(LocalDateTime.now().plusMinutes(15));
        HorizontalLayout finishLayout = new HorizontalLayout(finishPicker, isYandexOrder); // Чекбокс рядом с временем готовности
        finishLayout.setAlignItems(Alignment.CENTER);
        finishLayout.setWidthFull();

        Button createOrderButton = new Button("Создать заказ");
        Button clearCartButton = new Button("Очистить корзину");
        HorizontalLayout buttonBar = new HorizontalLayout(createOrderButton, clearCartButton);

        createOrderButton.addClickListener(e -> {
            if (cartItems.isEmpty()) {
                Notification.show("Корзина пуста, нельзя создать заказ");
                return;
            }

            String orderNumber = orderNumberField.getValue().trim();
            if (orderNumber.isEmpty()) {
                Notification.show("Нельзя создать заказ без номера");
                return;
            }

            LocalDateTime finishTime = finishPicker.getValue();
            if (finishTime == null) {
                Notification.show("Пожалуйста, укажите время готовности");
                return;
            }

            Instant kitchenShouldGetOrderAt = (selectedKitchenStart != null) ? selectedKitchenStart : Instant.now();
            Instant shouldBeFinishedAt = finishTime.atZone(ZoneId.systemDefault()).toInstant();

            if (shouldBeFinishedAt.isBefore(kitchenShouldGetOrderAt)) {
                Notification.show("Время готовности не может быть раньше времени начала приготовления");
                return;
            }

            List<MenuItem> itemsToCreate = new ArrayList<>();
            for (CartItem cartItem : cartItems) {
                for (int i = 0; i < cartItem.getQuantity(); i++) {
                    itemsToCreate.add(cartItem.getMenuItem());
                }
            }

            boolean yandexOrder = isYandexOrder.getValue(); // Получаем значение чекбокса
            viewService.createOrder(orderNumber, itemsToCreate, shouldBeFinishedAt, kitchenShouldGetOrderAt);
            Notification.show("Заказ создан! Номер: " + orderNumber + ", Позиции: " + itemsToCreate.size() +
                    (yandexOrder ? ", Яндекс заказ" : ""));

            cartItems.clear();
            updateTotalPay();
            updateTotalTime();
            chosenGrid.getDataProvider().refreshAll();
            updateExtrasList(
                    (VerticalLayout) ((HorizontalLayout) extrasLayout.getComponentAt(1)).getComponentAt(0),
                    (VerticalLayout) ((HorizontalLayout) extrasLayout.getComponentAt(1)).getComponentAt(1),
                    menuExtras
            );
            orderNumberField.clear();

            selectedKitchenStart = null;
            kitchenStartDisplay.setText("Время начала приготовления: Сейчас");
            finishPicker.setValue(LocalDateTime.now().plusMinutes(30));
            isYandexOrder.setValue(false); // Сбрасываем чекбокс после создания заказа
        });

        clearCartButton.addClickListener(e -> {
            cartItems.clear();
            updateTotalPay();
            updateTotalTime();
            chosenGrid.getDataProvider().refreshAll();
            updateExtrasList(
                    (VerticalLayout) ((HorizontalLayout) extrasLayout.getComponentAt(1)).getComponentAt(0),
                    (VerticalLayout) ((HorizontalLayout) extrasLayout.getComponentAt(1)).getComponentAt(1),
                    menuExtras
            );
            Notification.show("Корзина очищена");

            selectedKitchenStart = null;
            kitchenStartDisplay.setText("Время начала приготовления: Сейчас");
            isYandexOrder.setValue(false); // Сбрасываем чекбокс при очистке
        });

        cartLayout.add(chosenTitle, chosenGrid, kitchenStartLayout, finishLayout, totalPay, totalTime, buttonBar);
        return cartLayout;
    }

    private void updateTotalPay() {
        double total = cartItems.stream()
                .mapToDouble(cartItem -> cartItem.getMenuItem().getPrice() * cartItem.getQuantity())
                .sum();
        totalPay.setText("К оплате: " + total + " рублей");
    }

    private Div buildAllOrdersLayout() {
        Div ordersLayout = new Div();
        ordersLayout.setWidthFull();

        ordersGrid.removeAllColumns();

        ordersGrid.addColumn(OrderShortDto::getName).setHeader("ID").setAutoWidth(true);
        ordersGrid.addColumn(dto -> dto.getItems() == null ? 0 : dto.getItems().stream()
                .filter(item -> item.getStatus() != OrderItemStationStatus.CANCELED)
                .count()).setHeader("Кол-во").setAutoWidth(true);
        ordersGrid.addColumn(orderDto -> switch (orderDto.getStatus()) {
            case CREATED -> "Создан";
            case COOKING -> "Готовится";
            case COLLECTING -> "Сборка";
            case READY -> "Выполнен";
            case CANCELED -> "Отменён";
            default -> "";
        }).setHeader("Статус").setAutoWidth(true);
        ordersGrid.addColumn(order -> order.getKitchenShouldGetOrderAt()
                        .atZone(ZoneId.systemDefault())
                        .toLocalTime()
                        .format(DateTimeFormatter.ofPattern("HH:mm")))
                .setHeader("Начало").setAutoWidth(true);
        ordersGrid.addColumn(order -> order.getShouldBeFinishedAt()
                        .atZone(ZoneId.systemDefault())
                        .toLocalTime()
                        .format(DateTimeFormatter.ofPattern("HH:mm")))
                .setHeader("Готов к").setAutoWidth(true);
        ordersGrid.addComponentColumn(orderDto -> {
            HorizontalLayout layout = new HorizontalLayout();

            Button detailsBtn = new Button(VaadinIcon.LIST_OL.create());
            detailsBtn.addClickListener(e -> openOrderItemsDialog(orderDto));
            layout.add(detailsBtn);

            if (orderDto.getStatus() != OrderStatus.READY && orderDto.getStatus() != OrderStatus.CANCELED) {
                Button editBtn = new Button(VaadinIcon.CLOCK.create());
                editBtn.addClickListener(e -> openEditDialog(orderDto));
                layout.add(editBtn);

                Button cancelBtn = new Button(VaadinIcon.CLOSE.create());
                cancelBtn.addClickListener(e -> {
                    viewService.cancelOrder(orderDto.getId());
                    Notification.show("Заказ " + orderDto.getName() + " отменён!");
                    refreshOrdersGrid();
                });
                layout.add(cancelBtn);
            }
            return layout;
        }).setHeader("Действие").setAutoWidth(true);

        ordersLayout.add(new H3("Список всех заказов:"), ordersGrid);
        return ordersLayout;
    }

    private void openEditDialog(OrderShortDto orderDto) {
        Dialog editDialog = new Dialog();
        editDialog.setHeaderTitle("Редактировать время начала приготовления");

        DateTimePicker picker = new DateTimePicker("Время начала приготовления");
        picker.setValue(orderDto.getKitchenShouldGetOrderAt().atZone(ZoneId.systemDefault()).toLocalDateTime());

        Button saveBtn = new Button("Сохранить", ev -> {
            LocalDateTime newTime = picker.getValue();
            if (newTime != null) {
                Instant newInstant = newTime.atZone(ZoneId.systemDefault()).toInstant();
                viewService.updateKitchenShouldGetOrderAt(orderDto.getId(), newInstant);
                Notification.show("Время начала приготовления обновлено");
                editDialog.close();
                refreshOrdersGrid();
            } else {
                Notification.show("Пожалуйста, укажите время");
            }
        });

        Button cancelBtn = new Button("Отмена", ev -> editDialog.close());

        editDialog.add(picker);
        editDialog.getFooter().add(saveBtn, cancelBtn);
        editDialog.open();
    }

    private void refreshOrdersGrid() {
        List<OrderShortDto> allOrders = viewService.getAllOrdersWithItems();
        List<OrderShortDto> notCanceled = allOrders.stream()
                .filter(dto -> !OrderStatus.CANCELED.equals(dto.getStatus()))
                .collect(Collectors.toList());
        ordersGrid.setItems(notCanceled);
    }

    private void openOrderItemsDialog(OrderShortDto orderDto) {
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
            } else {
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
            } else {
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

        setsGrid.addComponentColumn(set -> {
            Button addButton = new Button("Добавить");
            addButton.addClickListener(click -> {
                for (MenuItem i : set.getMenuItems()) {
                    viewService.addItemToOrder(orderId, i);
                }
                Notification.show("Добавлен сет: " + set.getName());
                itemsGrid.setItems(this.viewService.getOrderItems(orderId));
            });
            return addButton;
        }).setHeader("Действие");

        setsGrid.setItems(menuItemCombos);

        setSearchField.addValueChangeListener(ev -> {
            String search = ev.getValue().toLowerCase().trim();
            if (search.isEmpty()) {
                setsGrid.setItems(menuItemCombos);
            } else {
                setsGrid.setItems(
                        menuItemCombos.stream()
                                .filter(set -> set.getName().toLowerCase().contains(search))
                                .collect(Collectors.toList())
                );
            }
        });

        setsLayout.add(setSearchField, setsGrid);

        Div tabsContent = new Div(rollsLayout, setsLayout);
        tabsContent.setSizeFull();

        VerticalLayout content = new VerticalLayout(tabs, tabsContent);
        content.setPadding(false);
        content.setSpacing(true);
        content.setSizeFull();

        addDialog.add(content);

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

    private void updateTotalTime() {
        int totalSeconds = cartItems.stream()
                .mapToInt(cartItem -> {
                    if (cartItem.getMenuItem().getTimeToCook() == null) {
                        return 0;
                    }
                    return Math.toIntExact(cartItem.getMenuItem().getTimeToCook().toSeconds()) * cartItem.getQuantity();
                })
                .sum();

        String formattedTime = (totalSeconds >= 60)
                ? String.format("%d мин : %02d сек", totalSeconds / 60, totalSeconds % 60)
                : String.format("%d сек", totalSeconds);

        totalTime.setText("Общее время приготовления: " + formattedTime);

        if (totalSeconds > 0) {
            Instant startTime = (selectedKitchenStart != null) ? selectedKitchenStart : Instant.now();
            finishPicker.setValue(startTime.atZone(ZoneId.systemDefault()).toLocalDateTime().plusSeconds(totalSeconds));
        }
    }

    private void openKitchenStartDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Выберите время начала приготовления");

        DateTimePicker picker = new DateTimePicker();
        picker.setValue(selectedKitchenStart != null
                ? selectedKitchenStart.atZone(ZoneId.systemDefault()).toLocalDateTime()
                : LocalDateTime.now());

        Button saveBtn = new Button("Сохранить", ev -> {
            LocalDateTime selectedTime = picker.getValue();
            if (selectedTime != null) {
                selectedKitchenStart = selectedTime.atZone(ZoneId.systemDefault()).toInstant();
                kitchenStartDisplay.setText("Время начала приготовления: " + selectedTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
                dialog.close();
            } else {
                Notification.show("Пожалуйста, выберите время");
            }
        });

        Button cancelBtn = new Button("Отмена", ev -> dialog.close());

        dialog.add(picker);
        dialog.getFooter().add(saveBtn, cancelBtn);
        dialog.open();
    }
}
