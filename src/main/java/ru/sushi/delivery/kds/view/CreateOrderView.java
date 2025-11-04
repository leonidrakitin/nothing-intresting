package ru.sushi.delivery.kds.view;

import com.vaadin.componentfactory.DateRange;
import com.vaadin.componentfactory.EnhancedDateRangePicker;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import ru.sushi.delivery.kds.config.CityProperties;
import ru.sushi.delivery.kds.domain.persist.entity.ItemCombo;
import ru.sushi.delivery.kds.domain.persist.entity.product.MenuItem;
import ru.sushi.delivery.kds.dto.OrderItemDto;
import ru.sushi.delivery.kds.dto.OrderShortDto;
import ru.sushi.delivery.kds.model.OrderItemStationStatus;
import ru.sushi.delivery.kds.model.OrderStatus;
import ru.sushi.delivery.kds.service.ViewService;
import ru.sushi.delivery.kds.service.listeners.CashListener;
import ru.sushi.delivery.kds.view.dto.CartItem;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Route("create")
public class CreateOrderView extends VerticalLayout {

    private final List<CartItem> cartItems = new ArrayList<>();
    private final ViewService viewService;
    private final CashListener cashListener;
    private final CityProperties cityProperties;
    private Instant selectedKitchenStart = null;
    private final Span kitchenStartDisplay = new Span("Время начала приготовления: Сейчас");
    private final Button changeKitchenStartButton = new Button("изменить");
    private final DateTimePicker finishPicker = new DateTimePicker("Время готовности");
    private final Checkbox isYandexOrder = new Checkbox("Заказ Яндекс");
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
    private final Grid<OrderShortDto> activeOrdersGrid = new Grid<>(OrderShortDto.class, false);
    private final TextField orderNumberField = new TextField("Номер заказа");
    private final H3 totalPay = new H3("К оплате: 0.0 рублей");
    private final H5 totalTime = new H5("Общее время приготовления: 0 минут");
    private final EnhancedDateRangePicker datePicker = new EnhancedDateRangePicker("Диапазон дат");
    private final ComboBox<OrderStatus> statusFilter = new ComboBox<>("Статус");
    private final Button applyDateFilterButton = new Button("Применить", VaadinIcon.CALENDAR.create());

    @Autowired
    public CreateOrderView(ViewService viewService, CashListener cashListener, CityProperties cityProperties) {
        setSizeFull();

        this.viewService = viewService;
        this.cashListener = cashListener;
        this.cityProperties = cityProperties;

        getStyle().set("padding", "20px");
        getStyle().set("gap", "20px");

        // Создаем заголовок с названием города
        H1 cityHeader = new H1(cityProperties.getCity());
        cityHeader.getStyle()
                .set("text-align", "center")
                .set("margin", "0 0 20px 0")
                .set("color", "var(--lumo-primary-color)")
                .set("font-size", "3rem")
                .set("font-weight", "bold")
                .set("text-shadow", "2px 2px 4px rgba(0,0,0,0.1)");

        this.menuMenuItems = viewService.getAllMenuItems();
        this.menuItemCombos = viewService.getAllCombos();
        this.menuExtras = viewService.getAllExtras();

        datePicker.setId("order-create-picker");
        datePicker.setWidth("300px"); // Устанавливаем явную ширину для видимости
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
        rollsSearchField.setValueChangeMode(ValueChangeMode.TIMEOUT);
//        rollsSearchField.setValueChangeTimeout(500); // Обновление через 500мс после остановки ввода
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
        setsSearchField.setValueChangeMode(ValueChangeMode.TIMEOUT);
//        setsSearchField.setValueChangeTimeout(500); // Обновление через 500мс после остановки ввода
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
        updateExtrasList(extrasListLeft, extrasListRight, menuExtras);

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
        Tab activeOrdersTab = new Tab("Активные заказы");
        Tab allOrdersTab = new Tab("Все заказы");
        Tabs rightTabs = new Tabs(cartTab, activeOrdersTab, allOrdersTab);

        Div cartLayout = buildCartLayout();
        Div activeOrdersLayout = buildActiveOrdersLayout();
        Div allOrdersLayout = buildAllOrdersLayout();
        activeOrdersLayout.setVisible(false);
        allOrdersLayout.setVisible(false);

        rightTabs.addSelectedChangeListener(event -> {
            if (event.getSelectedTab().equals(cartTab)) {
                cartLayout.setVisible(true);
                activeOrdersLayout.setVisible(false);
                allOrdersLayout.setVisible(false);
            } else if (event.getSelectedTab().equals(activeOrdersTab)) {
                cartLayout.setVisible(false);
                activeOrdersLayout.setVisible(true);
                allOrdersLayout.setVisible(false);
                refreshActiveOrdersGrid(null, null);
            } else {
                cartLayout.setVisible(false);
                activeOrdersLayout.setVisible(false);
                allOrdersLayout.setVisible(true);
                refreshOrdersGrid(null, null);
            }
        });

        VerticalLayout rightLayout = new VerticalLayout(rightTabs, orderNumberField, cartLayout, activeOrdersLayout, allOrdersLayout);
        rightLayout.setWidth("50%");
        rightLayout.setPadding(false);
        rightLayout.setSpacing(true);
        rightLayout.getStyle()
                .set("border", "1px solid #ccc")
                .set("border-radius", "8px")
                .set("padding", "20px");

        // Создаем горизонтальный контейнер для основного содержимого
        HorizontalLayout mainContent = new HorizontalLayout(leftLayout, rightLayout);
        mainContent.setSizeFull();
        mainContent.getStyle().set("gap", "20px");

        add(cityHeader, mainContent);
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
            Button addBtn = new Button(VaadinIcon.PLUS.create());
            addBtn.addClickListener(e -> {
                cartItem.increment();
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
            buttons.add(addBtn);
            return buttons;
        }).setHeader("Действие");
        chosenGrid.setItems(cartItems);

        Button updateTimeButton = new Button("Обновить", VaadinIcon.REFRESH.create());
        updateTimeButton.addClickListener(e -> updateKitchenStartTime());

        HorizontalLayout kitchenStartLayout = new HorizontalLayout(kitchenStartDisplay, updateTimeButton, changeKitchenStartButton);
        kitchenStartLayout.setAlignItems(Alignment.CENTER);
        changeKitchenStartButton.addClickListener(e -> openKitchenStartDialog());


        finishPicker.setValue(LocalDateTime.now().plusMinutes(15));

        // Инициализируем время начала приготовления актуальным временем
        selectedKitchenStart = Instant.now();
        kitchenStartDisplay.setText("Время начала приготовления: " +
                selectedKitchenStart.atZone(ZoneId.systemDefault()).toLocalDateTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));

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

            // Check if order with this name already exists today
            if (viewService.orderExistsByNameToday(orderNumber)) {
                Dialog errorDialog = new Dialog();
                errorDialog.setHeaderTitle("Ошибка");
                errorDialog.add("Заказ с номером '" + orderNumber + "' уже существует за сегодня.");

                Button okBtn = new Button("OK", ev -> errorDialog.close());
                Button cancelBtn = new Button("Отмена", ev -> errorDialog.close());

                errorDialog.getFooter().add(cancelBtn, okBtn);
                errorDialog.open();
                return;
            }

            if (selectedKitchenStart.isBefore(Instant.now())) {
                selectedKitchenStart = Instant.now();
                updateTotalTime();
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

            // Проверяем наличие приборов (productType = 7)
            boolean hasInstruments = cartItems.stream()
                    .anyMatch(cartItem -> cartItem.getMenuItem().getProductType().getId() == 7);

            if (!hasInstruments) {
                // Показываем диалог подтверждения
                Dialog confirmDialog = new Dialog();
                confirmDialog.setHeaderTitle("Точно без приборов?");
                confirmDialog.add("В корзине нет приборов. Продолжить создание заказа?");

                Button yesBtn = new Button("Да", ev -> {
                    confirmDialog.close();
                    createOrder(orderNumber, kitchenShouldGetOrderAt, shouldBeFinishedAt);
                });

                Button noBtn = new Button("Нет", ev -> confirmDialog.close());

                confirmDialog.getFooter().add(noBtn, yesBtn);
                confirmDialog.open();
            } else {
                createOrder(orderNumber, kitchenShouldGetOrderAt, shouldBeFinishedAt);
            }
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

            updateKitchenStartTime();
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

    private Div buildActiveOrdersLayout() {
        Div ordersLayout = new Div();
        ordersLayout.setWidthFull();

        Span ordersCountLabel = new Span("");

        activeOrdersGrid.removeAllColumns();
        activeOrdersGrid.addColumn(new ComponentRenderer<>(orderDto -> {
            HorizontalLayout layout = new HorizontalLayout();
            layout.setAlignItems(Alignment.CENTER);
            if (orderDto.getStatus() != OrderStatus.READY && orderDto.getStatus() != OrderStatus.CANCELED) {
                Icon icon = VaadinIcon.EXCLAMATION.create();
                icon.setColor("var(--lumo-error-color)");
                icon.setSize("var(--lumo-icon-size-s)");
                layout.add(icon);
            }
            layout.add(new Span(orderDto.getName()));
            return layout;
        })).setHeader("Номер").setAutoWidth(true);

        activeOrdersGrid.addColumn(dto -> dto.getItems() == null ? 0 : dto.getItems().stream()
                .filter(item -> item.getStatus() != OrderItemStationStatus.CANCELED)
                .count()).setHeader("Кол-во").setAutoWidth(true);
        activeOrdersGrid.addColumn(orderDto -> {
            String baseStatus = switch (orderDto.getStatus()) {
                case CREATED -> "Создан";
                case COOKING -> "Готовится";
                case COLLECTING -> "Сборка";
                case READY -> "Выполнен";
                case CANCELED -> "Отменён";
            };

            // Добавляем процент готовности для статуса "Готовится"
            if (orderDto.getStatus() == OrderStatus.COOKING) {
                int progressPercent = calculateOrderProgress(orderDto);
                return baseStatus + " (" + progressPercent + "%)";
            }

            return baseStatus;
        }).setHeader("Статус").setAutoWidth(true);
        activeOrdersGrid.addColumn(order -> order.getKitchenShouldGetOrderAt()
                        .atZone(ZoneId.systemDefault())
                        .toLocalTime()
                        .format(DateTimeFormatter.ofPattern("HH:mm")))
                .setHeader("Начало").setAutoWidth(true);
        activeOrdersGrid.addColumn(order -> order.getShouldBeFinishedAt()
                        .atZone(ZoneId.systemDefault())
                        .toLocalTime()
                        .format(DateTimeFormatter.ofPattern("HH:mm")))
                .setHeader("Готов к").setAutoWidth(true);
        activeOrdersGrid.addComponentColumn(orderDto -> {
            HorizontalLayout layout = new HorizontalLayout();

            Button detailsBtn = new Button(VaadinIcon.LIST_OL.create());
            detailsBtn.addClickListener(e -> openOrderItemsDialog(orderDto));
            layout.add(detailsBtn);

            if (orderDto.getStatus() != OrderStatus.READY && orderDto.getStatus() != OrderStatus.CANCELED) {
                Button editTimeBtn = new Button(VaadinIcon.CLOCK.create());
                editTimeBtn.addClickListener(e -> openEditDialog(orderDto));
                layout.add(editTimeBtn);

                Button editNameBtn = new Button(VaadinIcon.EDIT.create());
                editNameBtn.addClickListener(e -> openEditOrderNameDialog(orderDto));
                layout.add(editNameBtn);

                Button priorityBtn = new Button(VaadinIcon.ARROW_UP.create());
                priorityBtn.addClickListener(e -> setPriorityTimeForOrder(orderDto));
                layout.add(priorityBtn);

                Button cancelBtn = new Button(VaadinIcon.CLOSE.create());
                cancelBtn.addClickListener(e -> {
                    viewService.cancelOrder(orderDto.getId());
                    Notification.show("Заказ " + orderDto.getName() + " отменён!");
                    refreshActiveOrdersGrid(null, ordersCountLabel);
                });
                layout.add(cancelBtn);
            }
            return layout;
        }).setHeader("Действие").setAutoWidth(true);

        ordersLayout.add(new H3("Активные заказы:"), activeOrdersGrid, ordersCountLabel);
        refreshActiveOrdersGrid(null, ordersCountLabel);

        return ordersLayout;
    }

    private Div buildAllOrdersLayout() {
        Div ordersLayout = new Div();
        ordersLayout.setWidthFull();

        // Фильтр по датам
        datePicker.setValue(new DateRange(LocalDate.now(), null));
        datePicker.setWidth("300px");
        datePicker.getStyle().set("display", "inline-block");

        statusFilter.setItems(OrderStatus.values());
        statusFilter.setItemLabelGenerator(status -> switch (status) {
            case CREATED -> "Создан";
            case COOKING -> "Готовится";
            case COLLECTING -> "Сборка";
            case READY -> "Выполнен";
            case CANCELED -> "Отменён";
        });
        statusFilter.setPlaceholder("Все статусы");

        Span ordersCountLabel = new Span("");

        // Кнопка применения фильтров
        applyDateFilterButton.addClickListener(e -> refreshOrdersGrid(statusFilter.getValue(), ordersCountLabel));

        HorizontalLayout filterLayout = new HorizontalLayout(statusFilter, datePicker, applyDateFilterButton);
        filterLayout.setAlignItems(Alignment.BASELINE);
        filterLayout.setWidthFull();
        filterLayout.setSpacing(true);

        ordersGrid.removeAllColumns();
        ordersGrid.addColumn(new ComponentRenderer<>(orderDto -> {
            HorizontalLayout layout = new HorizontalLayout();
            layout.setAlignItems(Alignment.CENTER);
            if (orderDto.getStatus() != OrderStatus.READY && orderDto.getStatus() != OrderStatus.CANCELED) {
                Icon icon = VaadinIcon.EXCLAMATION.create();
                icon.setColor("var(--lumo-error-color)");
                icon.setSize("var(--lumo-icon-size-s)");
                layout.add(icon);
            }
            layout.add(new Span(orderDto.getName()));
            return layout;
        })).setHeader("Номер").setAutoWidth(true);

        ordersGrid.addColumn(dto -> dto.getItems() == null ? 0 : dto.getItems().stream()
                .filter(item -> item.getStatus() != OrderItemStationStatus.CANCELED)
                .count()).setHeader("Кол-во").setAutoWidth(true);
        ordersGrid.addColumn(orderDto -> {
            String baseStatus = switch (orderDto.getStatus()) {
                case CREATED -> "Создан";
                case COOKING -> "Готовится";
                case COLLECTING -> "Сборка";
                case READY -> "Выполнен";
                case CANCELED -> "Отменён";
            };

            // Добавляем процент готовности для статуса "Готовится"
            if (orderDto.getStatus() == OrderStatus.COOKING) {
                int progressPercent = calculateOrderProgress(orderDto);
                return baseStatus + " (" + progressPercent + "%)";
            }

            return baseStatus;
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
                Button editTimeBtn = new Button(VaadinIcon.CLOCK.create());
                editTimeBtn.addClickListener(e -> openEditDialog(orderDto));
                layout.add(editTimeBtn);

                Button editNameBtn = new Button(VaadinIcon.EDIT.create());
                editNameBtn.addClickListener(e -> openEditOrderNameDialog(orderDto));
                layout.add(editNameBtn);

                Button priorityBtn = new Button(VaadinIcon.ARROW_UP.create());
                priorityBtn.addClickListener(e -> setPriorityTimeForOrder(orderDto));
                layout.add(priorityBtn);

                Button cancelBtn = new Button(VaadinIcon.CLOSE.create());
                cancelBtn.addClickListener(e -> {
                    viewService.cancelOrder(orderDto.getId());
                    Notification.show("Заказ " + orderDto.getName() + " отменён!");
                    refreshAllOrdersTables();
                });
                layout.add(cancelBtn);
            } else if (orderDto.getStatus() == OrderStatus.READY) {
                // Для выполненных заказов показываем кнопки редактирования и возврата
                Button editNameBtn = new Button(VaadinIcon.EDIT.create());
                editNameBtn.addClickListener(e -> openEditOrderNameDialog(orderDto));
                layout.add(editNameBtn);

                Button returnBtn = new Button("Вернуть");
                returnBtn.addClickListener(e -> openReturnItemsDialog(orderDto));
                layout.add(returnBtn);
            }
            return layout;
        }).setHeader("Действие").setAutoWidth(true);

        ordersLayout.add(new H3("Список всех заказов:"), filterLayout, ordersGrid, ordersCountLabel);
        refreshOrdersGrid(statusFilter.getValue(), ordersCountLabel);

        return ordersLayout;
    }

    private void openEditDialog(OrderShortDto orderDto) {
        Dialog editDialog = new Dialog();
        editDialog.setHeaderTitle("Редактировать время начала приготовления");

        DateTimePicker picker = new DateTimePicker("Время начала приготовления");
        picker.setLocale(Locale.of("ru", "RU"));

        picker.setValue(orderDto.getKitchenShouldGetOrderAt().atZone(ZoneId.systemDefault()).toLocalDateTime());

        Button saveBtn = new Button("Сохранить", ev -> {
            LocalDateTime newTime = picker.getValue();
            if (newTime != null) {
                Instant newInstant = newTime.atZone(ZoneId.systemDefault()).toInstant();
                viewService.updateKitchenShouldGetOrderAt(orderDto.getId(), newInstant);
                Notification.show("Время начала приготовления обновлено");
                editDialog.close();
                refreshAllOrdersTables();
            } else {
                Notification.show("Пожалуйста, укажите время");
            }
        });

        Button cancelBtn = new Button("Отмена", ev -> editDialog.close());

        editDialog.add(picker);
        editDialog.getFooter().add(saveBtn, cancelBtn);
        editDialog.open();
    }

    private void refreshActiveOrdersGrid(OrderStatus statusFilter, Span ordersCountLabel) {
        List<OrderShortDto> allOrders = viewService.getAllOrdersWithItems();

        // Обновляем таблицу
        activeOrdersGrid.setItems(allOrders);

        if (ordersCountLabel != null) {
            ordersCountLabel.setText("Заказов: " + allOrders.size());
        }
    }

    private void refreshAllOrdersTables() {
        // Обновляем активные заказы если видима их вкладка
        if (activeOrdersGrid.isAttached()) {
            refreshActiveOrdersGrid(null, null);
        }
        // Обновляем все заказы если видима их вкладка
        if (ordersGrid.isAttached()) {
            refreshOrdersGrid(null, null);
        }
    }

    private void refreshOrdersGrid(OrderStatus statusFilter, Span ordersCountLabel) {
        LocalDate from = datePicker.getValue() != null ? datePicker.getValue().getStartDate() : LocalDate.now();
        LocalDate to = datePicker.getValue() != null ? datePicker.getValue().getEndDate() : LocalDate.now();

        if (to == null) {
            to = from;
        }

        if (from.isAfter(to)) {
            Notification.show("Дата 'С' не может быть позже даты 'По'");
            return;
        }

        List<OrderShortDto> allOrders = viewService.getAllOrdersWithItemsBetweenDates(
                from.atStartOfDay(ZoneId.systemDefault()).toInstant(),
                to.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
        );

        // Применяем фильтр по статусу, если он выбран
        if (statusFilter != null) {
            allOrders = allOrders.stream()
                    .filter(order -> order.getStatus() == statusFilter)
                    .collect(Collectors.toList());
        }

        // Обновляем таблицу
        ordersGrid.setItems(allOrders);

        if (ordersCountLabel != null) {
            ordersCountLabel.setText("Заказов: " + allOrders.size());
        }
    }

    private void openOrderItemsDialog(OrderShortDto orderDto) {
        Dialog dialog = new Dialog();
        dialog.setWidth("600px");
        dialog.setHeaderTitle("Позиции заказа: " + orderDto.getName());

        Grid<OrderItemDto> itemsGrid = new Grid<>(OrderItemDto.class, false);
        itemsGrid.addColumn(OrderItemDto::getName).setHeader("Наименование");

        // Добавляем столбцы станции и статуса для незавершенных заказов
        if (!OrderStatus.READY.name().equalsIgnoreCase(orderDto.getStatus().toString())) {
            itemsGrid.addColumn(itemDto -> itemDto.getCurrentStation() != null ? itemDto.getCurrentStation().getName() : "")
                    .setHeader("Станция").setAutoWidth(true);
            itemsGrid.addColumn(itemDto -> switch (itemDto.getStatus()) {
                case ADDED -> "Ожидает";
                case STARTED -> "Начато";
                case COMPLETED -> "Завершено";
                case CANCELED -> "Отменено";
            }).setHeader("Статус").setAutoWidth(true);
        }

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
        rollSearchField.setValueChangeMode(ValueChangeMode.TIMEOUT);
//        rollSearchField.setValueChangeTimeout(500); // Обновление через 500мс после остановки ввода

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
        setSearchField.setValueChangeMode(ValueChangeMode.TIMEOUT);
//        setSearchField.setValueChangeTimeout(500); // Обновление через 500мс после остановки ввода

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
//        this.cashListener.register(this);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
//        this.cashListener.unregister(this);
        super.onDetach(detachEvent);
    }

//    @Override
//    public void receiveBroadcast(BroadcastMessage message) {
//        // Обрабатываем только уведомления, игнорируем REFRESH_PAGE для страницы создания заказов
//        if (message.getType() == BroadcastMessageType.NOTIFICATION) {
//            Notification.show(message.getContent());
//        } else if (message.getType() == BroadcastMessageType.REFRESH_PAGE) {
//            // Логируем получение REFRESH_PAGE сообщений для диагностики
//            System.out.println("CreateOrderView получил REFRESH_PAGE сообщение - это не должно происходить!");
//            // Игнорируем REFRESH_PAGE сообщения
//        }
//        // REFRESH_PAGE сообщения игнорируем - страница создания заказов не должна обновляться автоматически
//    }

    private void updateTotalTime() {
        // Подсчитываем количество позиций, исключая ProductType с id 7, 8, 9
        int mainItemsCount = cartItems.stream()
                .mapToInt(cartItem -> {
                    Long productTypeId = cartItem.getMenuItem().getProductType().getId();
                    // Исключаем ProductType с id 7, 8, 9
                    if (productTypeId == 7 || productTypeId == 8 || productTypeId == 9) {
                        return 0;
                    }
                    return cartItem.getQuantity();
                })
                .sum();

        // Определяем время приготовления по новым правилам
        int totalMinutes;
        if (mainItemsCount <= 2) {
            totalMinutes = 10;
        } else if (mainItemsCount <= 4) {
            totalMinutes = 15;
        } else {
            totalMinutes = 20;
        }

        String formattedTime = String.format("%d мин", totalMinutes);
        totalTime.setText("Общее время приготовления: " + formattedTime);

        // Обновляем время готовности (без обновления времени начала, чтобы избежать постоянных перерисовок)
        Instant startTime = (selectedKitchenStart != null) ? selectedKitchenStart : Instant.now();
        finishPicker.setValue(startTime.atZone(ZoneId.systemDefault()).toLocalDateTime().plusMinutes(totalMinutes));
    }

    private void updateKitchenStartTime() {
        // Обновляем время начала приготовления на текущее время
        selectedKitchenStart = Instant.now();
        LocalDateTime currentTime = selectedKitchenStart.atZone(ZoneId.systemDefault()).toLocalDateTime();
        kitchenStartDisplay.setText("Время начала приготовления: " +
                currentTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
        updateTotalTime();
    }

    private void createOrder(String orderNumber, Instant kitchenShouldGetOrderAt, Instant shouldBeFinishedAt) {
        List<MenuItem> itemsToCreate = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            for (int i = 0; i < cartItem.getQuantity(); i++) {
                itemsToCreate.add(cartItem.getMenuItem());
            }
        }

        boolean yandexOrder = isYandexOrder.getValue();
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

        updateKitchenStartTime();
        finishPicker.setValue(LocalDateTime.now().plusMinutes(30));
        isYandexOrder.setValue(false);
    }

    private void openKitchenStartDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Выберите время начала приготовления");

        DateTimePicker picker = new DateTimePicker();
        picker.setLocale(Locale.of("ru", "RU"));

        picker.setValue(selectedKitchenStart != null
                ? selectedKitchenStart.atZone(ZoneId.systemDefault()).toLocalDateTime()
                : LocalDateTime.now());

        Button saveBtn = new Button("Сохранить", ev -> {
            LocalDateTime selectedTime = picker.getValue();
            if (selectedTime != null) {
                selectedKitchenStart = selectedTime.atZone(ZoneId.systemDefault()).toInstant();
                kitchenStartDisplay.setText("Время начала приготовления: " + selectedTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
                updateTotalTime();
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

    private void openEditOrderNameDialog(OrderShortDto orderDto) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Редактировать номер заказа: " + orderDto.getName());

        TextField orderNameField = new TextField("Номер заказа");
        orderNameField.setValue(orderDto.getName());
        orderNameField.setWidthFull();
        orderNameField.setPlaceholder("Введите номер заказа...");

        Button saveBtn = new Button("Сохранить", ev -> {
            String newOrderName = orderNameField.getValue().trim();
            if (newOrderName.isEmpty()) {
                Notification.show("Номер заказа не может быть пустым");
                return;
            }

            try {
                viewService.updateOrderName(orderDto.getId(), newOrderName);
                Notification.show("Номер заказа обновлен: " + newOrderName);
                dialog.close();
                refreshAllOrdersTables();
            } catch (Exception e) {
                Notification.show("Ошибка при обновлении номера заказа: " + e.getMessage());
            }
        });

        Button cancelBtn = new Button("Отмена", ev -> dialog.close());

        VerticalLayout layout = new VerticalLayout(orderNameField);
        layout.setPadding(false);
        layout.setSpacing(true);

        dialog.add(layout);
        dialog.getFooter().add(saveBtn, cancelBtn);
        dialog.open();
    }

    private void openReturnItemsDialog(OrderShortDto orderDto) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Вернуть блюда из заказа #" + orderDto.getName());

        // Получаем все позиции заказа
        List<OrderItemDto> items = viewService.getOrderItems(orderDto.getId());

        // Фильтруем только основные блюда (исключаем допы и отмененные)
        List<OrderItemDto> itemsToShow = items.stream()
                .filter(item -> !item.isExtra() && item.getStatus() != OrderItemStationStatus.CANCELED)
                .collect(Collectors.toList());

        if (itemsToShow.isEmpty()) {
            Notification.show("Нет позиций для возврата");
            return;
        }

        // Создаем чекбоксы для каждой позиции (по умолчанию все выбраны)
        Map<Long, Boolean> selectedItems = new HashMap<>();
        itemsToShow.forEach(item -> selectedItems.put(item.getId(), true));

        VerticalLayout contentLayout = new VerticalLayout();

        Grid<OrderItemDto> itemsGrid = new Grid<>(OrderItemDto.class, false);
        itemsGrid.addColumn(OrderItemDto::getName).setHeader("Наименование");
        itemsGrid.addComponentColumn(item -> {
            Checkbox checkbox = new Checkbox(
                    item.getName(),
                    selectedItems.get(item.getId()),
                    e -> selectedItems.put(item.getId(), e.getValue()));
            return checkbox;
        }).setHeader("Выбрать");

        itemsGrid.setItems(itemsToShow);
        contentLayout.add(itemsGrid);

        dialog.add(contentLayout);

        Button returnBtn = new Button("Вернуть", ev -> {
            List<Long> selectedIds = selectedItems.entrySet().stream()
                    .filter(Map.Entry::getValue)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            if (selectedIds.isEmpty()) {
                Notification.show("Выберите хотя бы одну позицию");
                return;
            }

            try {
                viewService.returnOrderItems(orderDto.getId(), selectedIds);
                Notification.show("Позиции возвращены");
                dialog.close();
                refreshAllOrdersTables();
            } catch (Exception ex) {
                Notification.show("Ошибка при возврате позиций: " + ex.getMessage());
            }
        });

        Button cancelBtn = new Button("Отмена", ev -> dialog.close());
        dialog.getFooter().add(cancelBtn, returnBtn);
        dialog.open();
    }

    private void setPriorityTimeForOrder(OrderShortDto orderDto) {
        try {
            // Находим первый заказ в статусе COOKING (готовится)
            OrderShortDto firstCookingOrder = viewService.getAllActiveCollectorOrdersWithItems().stream()
                    .filter(order -> order.getStatus() == OrderStatus.COOKING)
                    .min(Comparator.comparing(OrderShortDto::getKitchenShouldGetOrderAt))
                    .orElse(null);

            if (firstCookingOrder != null) {
                // Устанавливаем время начала приготовления сразу после первого заказа в статусе COOKING
                viewService.setPriorityForOrderAfterCooking(orderDto.getId());
                Notification.show("Приоритет установлен для заказа " + orderDto.getName() + "! Заказ будет готовиться сразу после " + firstCookingOrder.getName());
            } else {
                // Если нет заказов в статусе COOKING, ищем первый заказ в статусе CREATED
                OrderShortDto firstCreatedOrder = viewService.getAllActiveCollectorOrdersWithItems().stream()
                        .filter(order -> order.getStatus() == OrderStatus.CREATED)
                        .min(Comparator.comparing(OrderShortDto::getKitchenShouldGetOrderAt))
                        .orElse(null);

                if (firstCreatedOrder != null) {
                    viewService.setPriorityForOrderAfterCooking(orderDto.getId());
                    Notification.show("Приоритет установлен для заказа " + orderDto.getName() + "! Заказ будет готовиться сразу после " + firstCreatedOrder.getName());
                } else {
                    Notification.show("Нет активных заказов для установки приоритета");
                    return;
                }
            }

            // Обновляем таблицу заказов
            refreshAllOrdersTables();
        } catch (Exception e) {
            Notification.show("Ошибка при установке приоритета: " + e.getMessage());
        }
    }

    private int calculateOrderProgress(OrderShortDto orderDto) {
        if (orderDto.getItems() == null || orderDto.getItems().isEmpty()) {
            return 0;
        }

        long totalItems = orderDto.getItems().stream()
                .filter(item -> !item.isExtra())
                .filter(item -> item.getStatus() != OrderItemStationStatus.CANCELED)
                .count();

        if (totalItems == 0) {
            return 0;
        }

        double totalCoefficient = orderDto.getItems().stream()
                .filter(item -> !item.isExtra())
                .filter(item -> item.getStatus() != OrderItemStationStatus.CANCELED)
                .mapToDouble(item -> {
                    if (item.getCurrentStation() == null) {
                        return 0.0;
                    }
                    // Коэффициент = 1 если станция ID >= 4
                    if (item.getCurrentStation().getId() >= 4) {
                        return 1.0;
                    }
                    // Коэффициент = 0.5 если станция ID > 2 ИЛИ статус == STARTED
                    else if (item.getCurrentStation().getId() == 2 && item.getStatus() == OrderItemStationStatus.STARTED) {
                        return 0.3;
                    }
                    // Коэффициент = 0.5 если станция ID > 2 ИЛИ статус == STARTED
                    else if (item.getCurrentStation().getId() == 3 && item.getStatus() == OrderItemStationStatus.ADDED) {
                        if (item.getFlowId() == 3) {
                            return 0.0;
                        }
                        return 0.6;
                    }
                    // Коэффициент = 0.5 если станция ID > 2 ИЛИ статус == STARTED
                    else if (item.getCurrentStation().getId() == 3 && item.getStatus() == OrderItemStationStatus.STARTED) {
                        if (item.getFlowId() == 3) {
                            return 0.5;
                        }
                        return 0.8;
                    }
                    // Иначе коэффициент = 0
                    return 0.0;
                })
                .sum();

        // Процент = (сумма коэффициентов / количество items) * 100, округляем
        return (int) Math.round((totalCoefficient / totalItems) * 100);
    }
}
