package ru.sushi.delivery.kds.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
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
import ru.sushi.delivery.kds.service.MultiCityViewService;
import ru.sushi.delivery.kds.service.MultiCityViewService.City;
import ru.sushi.delivery.kds.view.dto.CartItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Route("create-new")
public class CreateNewOrderView extends VerticalLayout {

    private final MultiCityViewService multiCityViewService;
    
    // Текущий выбранный город
    private City currentCity = City.PARNAS;
    
    // Данные для текущего города
    private List<MenuItem> currentMenuItems = new ArrayList<>();
    private List<ItemCombo> currentCombos = new ArrayList<>();
    private List<MenuItem> currentExtras = new ArrayList<>();
    
    // Корзина (общая для обоих городов, но можно разделить)
    private final List<CartItem> cartItems = new ArrayList<>();
    
    // UI компоненты
    private final Grid<MenuItem> rollsGrid = new Grid<>(MenuItem.class, false);
    private final Grid<ItemCombo> setsGrid = new Grid<>(ItemCombo.class, false);
    private final Grid<CartItem> chosenGrid = new Grid<>(CartItem.class, false);
    private final TextField orderNumberField = new TextField("Номер заказа");
    private final H3 totalPay = new H3("К оплате: 0.0 рублей");
    private final DateTimePicker finishPicker = new DateTimePicker("Время готовности");
    private final Checkbox isYandexOrder = new Checkbox("Заказ Яндекс");
    
    // Вкладки для городов
    private final Tab parnasTab = new Tab("Парнас");
    private final Tab ukhtaTab = new Tab("Ухта");
    private final Tabs cityTabs = new Tabs(parnasTab, ukhtaTab);
    
    // Вкладки для меню (Роллы/Сеты)
    private final Tab rollsTab = new Tab("Роллы");
    private final Tab setsTab = new Tab("Сеты");
    private final Tabs menuTabs = new Tabs(rollsTab, setsTab);
    
    private final VerticalLayout rollsTabLayout = new VerticalLayout();
    private final VerticalLayout setsTabLayout = new VerticalLayout();

    @Autowired
    public CreateNewOrderView(MultiCityViewService multiCityViewService) {
        this.multiCityViewService = multiCityViewService;
        
        setSizeFull();
        getStyle().set("padding", "20px");
        getStyle().set("gap", "20px");
        
        // Заголовок
        H1 header = new H1("Создание заказа (Мульти-город)");
        header.getStyle()
                .set("text-align", "center")
                .set("margin", "0 0 20px 0")
                .set("color", "var(--lumo-primary-color)");
        
        // Вкладки для переключения между городами
        cityTabs.setWidthFull();
        cityTabs.addSelectedChangeListener(event -> {
            if (event.getSelectedTab().equals(parnasTab)) {
                switchCity(City.PARNAS);
            } else {
                switchCity(City.UKHTA);
            }
        });
        
        // Вкладки для меню (Роллы/Сеты)
        menuTabs.setWidthFull();
        menuTabs.addSelectedChangeListener(event -> {
            if (event.getSelectedTab().equals(rollsTab)) {
                rollsTabLayout.setVisible(true);
                setsTabLayout.setVisible(false);
            } else {
                rollsTabLayout.setVisible(false);
                setsTabLayout.setVisible(true);
            }
        });
        
        // Инициализация UI
        initializeMenuTabs();
        initializeCart();
        
        // Загружаем данные для Парнаса по умолчанию
        switchCity(City.PARNAS);
        
        // Основной layout
        HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setSizeFull();
        mainLayout.getStyle().set("gap", "20px");
        
        // Левая часть - меню
        VerticalLayout leftLayout = new VerticalLayout(cityTabs, menuTabs, rollsTabLayout, setsTabLayout);
        leftLayout.setWidth("50%");
        leftLayout.setPadding(false);
        leftLayout.setSpacing(true);
        leftLayout.getStyle()
                .set("border", "1px solid #ccc")
                .set("border-radius", "8px")
                .set("padding", "20px");
        
        // Правая часть - корзина
        VerticalLayout rightLayout = new VerticalLayout(
            new Span("Город: " + getCurrentCityName()),
            orderNumberField,
            finishPicker,
            isYandexOrder,
            chosenGrid,
            totalPay
        );
        rightLayout.setWidth("50%");
        rightLayout.setPadding(false);
        rightLayout.setSpacing(true);
        rightLayout.getStyle()
                .set("border", "1px solid #ccc")
                .set("border-radius", "8px")
                .set("padding", "20px");
        
        mainLayout.add(leftLayout, rightLayout);
        
        add(header, mainLayout);
    }
    
    private void switchCity(City city) {
        this.currentCity = city;
        
        // Загружаем данные для выбранного города
        try {
            currentMenuItems = multiCityViewService.getMenuItems(city);
            currentCombos = multiCityViewService.getCombos(city);
            currentExtras = multiCityViewService.getExtras(city);
            
            // Обновляем UI
            rollsGrid.setItems(currentMenuItems);
            setsGrid.setItems(currentCombos);
            
            // Обновляем отображение города в правой части
            Notification.show("Переключено на: " + getCurrentCityName());
        } catch (Exception e) {
            Notification.show("Ошибка загрузки данных для " + getCurrentCityName() + ": " + e.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }
    
    private String getCurrentCityName() {
        return currentCity == City.PARNAS ? "Парнас" : "Ухта";
    }
    
    private void initializeMenuTabs() {
        // Вкладка "Роллы"
        rollsTabLayout.setPadding(false);
        rollsTabLayout.setSpacing(true);
        rollsTabLayout.setVisible(true);
        
        TextField rollsSearchField = new TextField("Поиск по роллам");
        rollsSearchField.setPlaceholder("Введите название...");
        rollsSearchField.setWidthFull();
        rollsSearchField.setValueChangeMode(ValueChangeMode.TIMEOUT);
        rollsSearchField.addValueChangeListener(e -> {
            String searchValue = e.getValue().trim().toLowerCase();
            if (searchValue.isEmpty()) {
                rollsGrid.setItems(currentMenuItems);
            } else {
                rollsGrid.setItems(
                    currentMenuItems.stream()
                        .filter(item -> item.getName().toLowerCase().contains(searchValue))
                        .collect(Collectors.toList())
                );
            }
        });
        
        rollsGrid.addColumn(MenuItem::getName).setHeader("Наименование");
        rollsGrid.addColumn(MenuItem::getPrice).setHeader("Цена");
        rollsGrid.setWidthFull();
        rollsGrid.addItemClickListener(e -> addToCart(e.getItem()));
        
        rollsTabLayout.add(rollsSearchField, rollsGrid);
        
        // Вкладка "Сеты"
        setsTabLayout.setPadding(false);
        setsTabLayout.setSpacing(true);
        setsTabLayout.setVisible(false);
        
        TextField setsSearchField = new TextField("Поиск по сетам");
        setsSearchField.setPlaceholder("Введите название...");
        setsSearchField.setWidthFull();
        setsSearchField.setValueChangeMode(ValueChangeMode.TIMEOUT);
        setsSearchField.addValueChangeListener(e -> {
            String searchValue = e.getValue().trim().toLowerCase();
            if (searchValue.isEmpty()) {
                setsGrid.setItems(currentCombos);
            } else {
                setsGrid.setItems(
                    currentCombos.stream()
                        .filter(combo -> combo.getName().toLowerCase().contains(searchValue))
                        .collect(Collectors.toList())
                );
            }
        });
        
        setsGrid.addColumn(ItemCombo::getName).setHeader("Наименование");
        setsGrid.setWidthFull();
        setsGrid.addItemClickListener(e -> {
            ItemCombo clickedSet = e.getItem();
            // Добавляем все позиции из сета в корзину
            if (clickedSet.getMenuItems() != null) {
                for (MenuItem item : clickedSet.getMenuItems()) {
                    addToCart(item);
                }
            }
            Notification.show("Добавлен сет: " + clickedSet.getName());
        });
        
        setsTabLayout.add(setsSearchField, setsGrid);
    }
    
    private void initializeCart() {
        finishPicker.setLocale(Locale.of("ru", "RU"));
        
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
                chosenGrid.getDataProvider().refreshAll();
            });
            Button addBtn = new Button(VaadinIcon.PLUS.create());
            addBtn.addClickListener(e -> {
                cartItem.increment();
                updateTotalPay();
                chosenGrid.getDataProvider().refreshAll();
            });
            buttons.add(removeBtn, addBtn);
            return buttons;
        }).setHeader("Действие");
        chosenGrid.setItems(cartItems);
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
        Notification.show(String.format("Добавлен: %s - %.1f рублей", item.getName(), item.getPrice()));
        chosenGrid.getDataProvider().refreshAll();
    }
    
    private void updateTotalPay() {
        double total = cartItems.stream()
                .mapToDouble(cartItem -> cartItem.getMenuItem().getPrice() * cartItem.getQuantity())
                .sum();
        totalPay.setText(String.format("К оплате: %.2f рублей", total));
    }
}

