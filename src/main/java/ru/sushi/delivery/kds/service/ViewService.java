package ru.sushi.delivery.kds.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.persist.entity.ItemCombo;
import ru.sushi.delivery.kds.domain.persist.entity.OrderItem;
import ru.sushi.delivery.kds.domain.persist.entity.flow.Screen;
import ru.sushi.delivery.kds.domain.persist.entity.flow.Station;
import ru.sushi.delivery.kds.domain.persist.entity.product.MenuItem;
import ru.sushi.delivery.kds.domain.service.*;
import ru.sushi.delivery.kds.dto.KitchenDisplayInfoDto;
import ru.sushi.delivery.kds.dto.OrderFullDto;
import ru.sushi.delivery.kds.dto.OrderItemDto;

import java.time.Instant;
import java.util.ArrayList;
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

    public void createOrder(
            String name,
            List<MenuItem> menuItems,
            Instant shouldBeFinishedAt,
            Instant kitchenShouldGetOrderAt
    ) {
        this.orderService.createOrder(name, menuItems, shouldBeFinishedAt, kitchenShouldGetOrderAt);
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

    public List<OrderFullDto> getScreenOrderItems(Long screenId) {
        Screen screen = this.screenService.getOrThrow(screenId);
        return this.orderService.getAllItemsByStationId(screen);
    }

    public void updateStatus(Long orderItemId) {
        this.orderService.updateOrderItem(orderItemId);
    }

    public void updateKitchenShouldGetOrderAt(Long orderId, Instant newInstant) {
        this.orderService.updateKitchenShouldGetOrderAt(orderId, newInstant);
    }

    public List<OrderFullDto> getAllOrdersWithItems() {
        return this.orderService.getAllActiveOrdersWithItems();
    }

    public List<OrderFullDto> getAllKitchenOrdersWithItems() {
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
                .orderName(item.getOrder().getName())
                .orderId(item.getOrder().getId())
                .name(item.getMenuItem().getName())
                .ingredients(new ArrayList<>(this.ingredientService.getMenuItemIngredients(item.getMenuItem().getId())))
                .status(item.getStatus())
                .statusUpdatedAt(item.getStatusUpdatedAt())
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
}
