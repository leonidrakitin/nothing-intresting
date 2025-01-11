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
import ru.sushi.delivery.kds.domain.persist.entity.ItemSet;
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

    // Grids слева
    private final Grid<MenuItem> rollsGrid = new Grid<>(MenuItem.class, false);
    private final Grid<ItemSet> setsGrid = new Grid<>(ItemSet.class, false);

    // Списки из BusinessLogic (исходный список)
    private final List<MenuItem> menuMenuItems;
    private final List<ItemSet> menuItemSets;

    // Таблица «Корзины» (справа, первая вкладка)
    private final Grid<MenuItem> chosenGrid = new Grid<>(MenuItem.class, false);

    // Таблица «Все заказы» (справа, вторая вкладка)
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

        // Загружаем списки из бизнес-логики
        this.menuMenuItems = viewService.getAllMenuItems(); // Роллы
        this.menuItemSets = List.of();                  // Сеты (пример, для иллюстрации)

        // ----------------------------
        // ЛЕВАЯ ЧАСТЬ
        // ----------------------------

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
        rollsGrid.setWidthFull();
        rollsGrid.addItemClickListener(e -> {
            MenuItem clickedMenuItem = e.getItem();
            chosenMenuItems.add(clickedMenuItem);
            Notification.show("Добавлен: " + clickedMenuItem.getName());
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
            chosenMenuItems.addAll(clickedSet.getMenuItems());
            Notification.show("Добавлен сет: " + clickedSet.getName());
            chosenGrid.getDataProvider().refreshAll();
        });

        setsTabLayout.add(setsSearchField, setsGrid);

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
        chosenGrid.setItems(chosenMenuItems);

        Button createOrderButton = new Button("Создать заказ");
        Button clearCartButton = new Button("Очистить корзину");
        HorizontalLayout buttonBar = new HorizontalLayout(createOrderButton, clearCartButton);

        createOrderButton.addClickListener(e -> {
            if (chosenMenuItems.isEmpty()) {
                Notification.show("Корзина пуста, нельзя создать заказ");
                return;
            }

            // Проверяем, заполнено ли поле «Номер заказа»
            String orderNumber = orderNumberField.getValue().trim();
            if (orderNumber.isEmpty()) {
                Notification.show("Нельзя создать заказ без номера");
                return;
            }

            // Создаём заказ
            viewService.createOrder(orderNumber, chosenMenuItems);
            Notification.show("Заказ создан! Номер: " + orderNumber +
                ", Позиции: " + chosenMenuItems.size());

            // Очищаем корзину
            chosenMenuItems.clear();
            chosenGrid.getDataProvider().refreshAll();
            orderNumberField.clear();
        });

        clearCartButton.addClickListener(e -> {
            chosenMenuItems.clear();
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

        // Колонка "ID"
        ordersGrid.addColumn(OrderFullDto::getName)
            .setHeader("ID");

        // Колонка "Кол-во позиций"
        ordersGrid.addColumn(dto -> {
            if (dto.getItems() == null) return 0;
            return dto.getItems().stream()
                .filter(item -> item.getStatus() != OrderItemStationStatus.CANCELED)
                .count();
        }).setHeader("Кол-во позиций");

        // Колонка "Статус"
        ordersGrid.addColumn(orderDto -> switch (orderDto.getStatus()) {
            case CREATED    -> "Создан";
            case COOKING    -> "Готовится";
            case COLLECTING -> "Сборка";
            case READY      -> "Выполнен";
            case CANCELED   -> "Отменён";
            default           -> "";
        }).setHeader("Статус");

        // Колонка с кнопками "Позиции" и "Отменить"
        ordersGrid.addComponentColumn(orderDto -> {
            HorizontalLayout layout = new HorizontalLayout();

            Button detailsBtn = new Button("Позиции");
            detailsBtn.addClickListener(e -> openOrderItemsDialog(orderDto));
            layout.add(detailsBtn);

            if (orderDto.getStatus() != OrderStatus.READY && orderDto.getStatus() != OrderStatus.CANCELED) {
                Button cancelBtn = new Button("Отменить");
                cancelBtn.addClickListener(e -> {
                    // Отмена заказа
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

        // Убираем заказы со статусом CANCELED
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

        // Грид с позициями
        Grid<OrderItemDto> itemsGrid = new Grid<>(OrderItemDto.class, false);
        itemsGrid.addColumn(OrderItemDto::getName).setHeader("Наименование");

        // Если заказ не READY, добавляем кнопки «Удалить» и «Добавить позицию»
        if (!OrderStatus.READY.name().equalsIgnoreCase(orderDto.getStatus().toString())) {

            // Колонка «Удалить»
            itemsGrid.addComponentColumn(itemDto -> {
                // Если статус = CANCELED, возвращаем пустой Layout (или Label "Отменено" вместо кнопки)
                if ("CANCELED".equals(itemDto.getStatus().name())) {
                    // Можно вернуть, например, Label
                    return new Span("Отменено");
                }

                // Иначе показываем кнопку "Удалить"
                Button removeBtn = new Button("Удалить");
                removeBtn.addClickListener(click -> {
                    viewService.cancelOrderItem(itemDto.getId());
                    Notification.show("Позиция удалена (ID=" + itemDto.getId() + ")");
                    // Перезагружаем заказ
                    itemsGrid.setItems(this.viewService.getOrderItems(orderDto.getId()));
                });

                return removeBtn;
            }).setHeader("Действие");


            // Добавляем кнопку "Добавить позицию" (внизу диалога)
            Button addItemBtn = new Button("Добавить позицию");
            addItemBtn.addClickListener(ev -> {
                // Открываем дополнительный диалог с выбором из menuItems
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
     * @param orderId  строковый «ID заказа»
     * @param itemsGrid грид, который нужно обновить после добавления
     * @return Dialog
     */
    private Dialog buildAddItemsDialog(Long orderId, Grid<OrderItemDto> itemsGrid) {
        Dialog addDialog = new Dialog();
        addDialog.setWidth("600px");
        addDialog.setHeaderTitle("Выберите позицию для добавления");

        // ---------------------------
        // Tabs: «Роллы» / «Сеты»
        // ---------------------------
        Tab rollsTab = new Tab("Роллы");
        Tab setsTab = new Tab("Сеты");
        Tabs tabs = new Tabs(rollsTab, setsTab);

        // Два layout-а
        VerticalLayout rollsLayout = new VerticalLayout();
        VerticalLayout setsLayout = new VerticalLayout();

        // Изначально показываем только «Роллы»
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

        // ---------------------------
        // Поиск и Grid для «Роллов»
        // ---------------------------
        TextField rollSearchField = new TextField("Поиск по роллам");
        rollSearchField.setPlaceholder("Введите название...");
        rollSearchField.setValueChangeMode(ValueChangeMode.EAGER);

        Grid<MenuItem> rollsGrid = new Grid<>(MenuItem.class, false);
        rollsGrid.setWidthFull();
        rollsGrid.addColumn(MenuItem::getName).setHeader("Наименование");

        // Добавляем колонку с кнопкой «Добавить»
        rollsGrid.addComponentColumn(item -> {
            Button addButton = new Button("Добавить");
            addButton.addClickListener(click -> {
                viewService.addItemToOrder(orderId, item);
                Notification.show("Добавлено: " + item.getName());

                // Обновляем основной грид позиций
                itemsGrid.setItems(this.viewService.getOrderItems(orderId));
            });
            return addButton;
        }).setHeader("Действие");

        rollsGrid.setItems(menuMenuItems); // Изначально все

        // Фильтрация при вводе
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

        // ---------------------------
        // Поиск и Grid для «Сетов»
        // ---------------------------
        TextField setSearchField = new TextField("Поиск по сетам");
        setSearchField.setPlaceholder("Введите название...");
        setSearchField.setValueChangeMode(ValueChangeMode.EAGER);

        Grid<ItemSet> setsGrid = new Grid<>(ItemSet.class, false);
        setsGrid.setWidthFull();
        setsGrid.addColumn(ItemSet::getName).setHeader("Наименование");

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

        setsGrid.setItems(menuItemSets);

        // Фильтрация при вводе
        setSearchField.addValueChangeListener(ev -> {
            String search = ev.getValue().toLowerCase().trim();
            if (search.isEmpty()) {
                setsGrid.setItems(menuItemSets);
            } else {
                setsGrid.setItems(
                    menuItemSets.stream()
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
