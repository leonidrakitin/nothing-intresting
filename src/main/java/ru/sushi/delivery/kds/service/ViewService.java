package ru.sushi.delivery.kds.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.persist.entity.ItemCombo;
import ru.sushi.delivery.kds.domain.persist.entity.OrderItem;
import ru.sushi.delivery.kds.domain.persist.entity.flow.Screen;
import ru.sushi.delivery.kds.domain.persist.entity.flow.Station;
import ru.sushi.delivery.kds.domain.persist.entity.product.MenuItem;
import ru.sushi.delivery.kds.domain.service.FlowCacheService;
import ru.sushi.delivery.kds.domain.service.IngredientService;
import ru.sushi.delivery.kds.domain.service.ItemComboService;
import ru.sushi.delivery.kds.domain.service.MenuItemService;
import ru.sushi.delivery.kds.domain.service.OrderService;
import ru.sushi.delivery.kds.domain.service.ScreenService;
import ru.sushi.delivery.kds.domain.persist.entity.OrderAddress;
import ru.sushi.delivery.kds.dto.KitchenDisplayInfoDto;
import ru.sushi.delivery.kds.dto.OrderAddressDto;
import ru.sushi.delivery.kds.dto.OrderItemDto;
import ru.sushi.delivery.kds.dto.OrderShortDto;
import ru.sushi.delivery.kds.dto.OrderTimelineDto;
import ru.sushi.delivery.kds.model.OrderType;
import ru.sushi.delivery.kds.model.PaymentType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ViewService {

    private final OrderService orderService;
    private final ScreenService screenService;
    private final MenuItemService menuItemService;
    private final IngredientService ingredientService;
    private final FlowCacheService flowCacheService; //todo remove
    private final ItemComboService itemComboService;

    public boolean orderExistsByNameToday(String name) {
        return this.orderService.orderExistsByNameToday(name);
    }

    public void createOrder(
            String name,
            List<MenuItem> menuItems,
            Instant shouldBeFinishedAt,
            Instant kitchenShouldGetOrderAt,
            OrderType orderType,
            OrderAddressDto address,
            String customerPhone,
            PaymentType paymentType,
            Instant deliveryTime
    ) {
        OrderAddress addressEntity = address != null ? address.toEntity() : null;
        this.orderService.createOrder(name, menuItems, shouldBeFinishedAt, kitchenShouldGetOrderAt,
                orderType != null ? orderType : OrderType.PICKUP, addressEntity, customerPhone, paymentType, deliveryTime);
    }

    public List<MenuItem> getAllMenuItems() {
        return this.menuItemService.getAllMenuItems();
    }

    public List<ItemCombo> getAllCombos() {
        return this.itemComboService.findAll();
    }

    public List<MenuItem> getAllExtras() {
        return this.menuItemService.getAllExtras();
    }

    public List<KitchenDisplayInfoDto> getAvailableDisplaysData() {
        List<KitchenDisplayInfoDto> kitchenDisplayData = new ArrayList<>();
        for (Screen screen : this.screenService.getAll()) {
            kitchenDisplayData.add(new KitchenDisplayInfoDto(screen.getId(), screen.getStation().getName()));
        }
        return kitchenDisplayData;
    }

    public Optional<Long> getScreenStationIfExists(Long screenId) {
        return this.screenService.get(screenId).map(Screen::getStation).map(Station::getId);
    }

    public List<OrderShortDto> getScreenOrderItems(Long screenId) {
        Screen screen = this.screenService.getOrThrow(screenId);
        return this.orderService.getAllItemsByStationId(screen);
    }

    public void updateStatus(Long orderItemId) {
        this.orderService.updateOrderItem(orderItemId);
    }

    public void updateKitchenShouldGetOrderAt(Long orderId, Instant newInstant) {
        this.orderService.updateKitchenShouldGetOrderAt(orderId, newInstant);
    }

    public List<OrderShortDto> getAllOrdersWithItems() {
        return this.orderService.getAllActiveOrdersWithItems();
    }

    public List<OrderShortDto> getAllActiveCollectorOrdersWithItems() {
        return this.orderService.getAllActiveCollectorOrdersWithItems();
    }

    public List<OrderShortDto> getAllOrdersWithItemsBetweenDates(Instant from, Instant to) {
        return this.orderService.getAllOrdersWithItemsBetweenDates(from, to);
    }

    public List<OrderShortDto> getAllKitchenOrdersWithItems() {
        return this.orderService.getAllKitchenOrdersWithItems();
    }

    public List<OrderItemDto> getOrderItems(Long orderId) {
        return this.orderService.getOrderItems(orderId).stream()
                .map(this::buildOrderItemDto)
                .toList();
    }

    public void updateAllOrderItemsToDone(Long orderId) {
        this.orderService.updateAllOrderItemsToDone(orderId);
    }

    public void cancelOrderItem(Long orderItemId) {
        this.orderService.cancelOrderItem(orderItemId);
    }

    public void addItemToOrder(Long orderId, MenuItem menuItem) {
        this.orderService.createOrderItem(orderId, menuItem);
    }

    public void cancelOrder(Long orderId) {
        this.orderService.cancelOrder(orderId);
    }

    private OrderItemDto buildOrderItemDto(OrderItem item) {
        return OrderItemDto.builder()
                .id(item.getId())
                .flowId(item.getMenuItem().getFlow().getId())
                .orderName(item.getOrder().getName())
                .orderId(item.getOrder().getId())
                .name(item.getMenuItem().getName())
                .ingredients(new ArrayList<>(this.ingredientService.getMenuItemIngredients(item.getMenuItem().getId())))
                .status(item.getStatus())
                .statusUpdatedAt(item.getStatusUpdatedAt())
                .createdAt(item.getStatusUpdatedAt())
                .timeToCook(180)
                //todo remove
                .currentStation(
                        this.flowCacheService.getStep(item.getMenuItem().getFlow().getId(), item.getCurrentFlowStep())
                                .getStation()
                )
                //todo remove
                .flowStepType(
                        this.flowCacheService.getStep(item.getMenuItem().getFlow().getId(), item.getCurrentFlowStep())
                                .getStepType()
                )
                .extra(item.getMenuItem().getProductType().isExtra())
                .build();
    }

    public void updateOrderName(Long orderId, String newName) {
        orderService.updateOrderName(orderId, newName);
    }

    public void setPriorityForOrder(Long orderId, OrderShortDto firstCookingOrder) {
        if (firstCookingOrder != null) {
            // Устанавливаем время начала приготовления чуть раньше найденного заказа
            Instant priorityTime = firstCookingOrder.getKitchenShouldGetOrderAt().minusSeconds(30); // на 30 секунд раньше
            
            // Обновляем время начала приготовления для выбранного заказа
            orderService.updateKitchenShouldGetOrderAt(orderId, priorityTime);
        } else {
            throw new RuntimeException("Нет заказов в статусе 'Готовится' для установки приоритета");
        }
    }

    public void setPriorityForOrderAfterCooking(Long orderId) {
        // Получаем все активные заказы, исключая текущий, и сортируем по времени
        List<OrderShortDto> activeOrders = getAllOrdersWithItems().stream()
                .filter(order -> !order.getId().equals(orderId)) // Исключаем текущий заказ
                .sorted(Comparator.comparing(OrderShortDto::getKitchenShouldGetOrderAt))
                .toList();
        
        if (activeOrders.isEmpty()) {
            // Если нет заказов - ничего не делаем
            return;
        }
        
        OrderShortDto firstOrder = activeOrders.get(0);
        Instant priorityTime;
        
        if (activeOrders.size() >= 2) {
            // Есть второй заказ - находим середину между ними
            OrderShortDto secondOrder = activeOrders.get(1);
            Instant firstTime = firstOrder.getKitchenShouldGetOrderAt();
            Instant secondTime = secondOrder.getKitchenShouldGetOrderAt();
            
            // Вычисляем разницу и делим пополам
            long timeDiff = secondTime.getEpochSecond() - firstTime.getEpochSecond();
            priorityTime = firstTime.plusSeconds(timeDiff / 2);
        } else {
            // Нет второго заказа - добавляем 1 минуту к первому
            priorityTime = firstOrder.getKitchenShouldGetOrderAt().plusSeconds(65);
        }
        
        orderService.updateKitchenShouldGetOrderAt(orderId, priorityTime);
    }

    public List<OrderShortDto> getAllHistoryOrdersWithItemsToday() {
        return this.orderService.getAllHistoryOrdersWithItemsToday();
    }

    public void returnOrderItems(Long orderId, List<Long> orderItemIds) {
        this.orderService.returnOrderItems(orderId, orderItemIds);
    }

    public void updateItemToCollecting(Long orderItemId) {
        this.orderService.updateItemToCollecting(orderItemId);
    }

    public Optional<OrderTimelineDto> getOrderTimeline(String orderName) {
        return this.orderService.getOrderTimeline(orderName);
    }
}
