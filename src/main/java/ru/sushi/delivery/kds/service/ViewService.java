package ru.sushi.delivery.kds.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.persist.entity.ItemCombo;
import ru.sushi.delivery.kds.domain.persist.entity.OrderItem;
import ru.sushi.delivery.kds.domain.persist.entity.flow.Screen;
import ru.sushi.delivery.kds.domain.persist.entity.flow.Station;
import ru.sushi.delivery.kds.domain.persist.entity.product.Meal;
import ru.sushi.delivery.kds.domain.service.*;
import ru.sushi.delivery.kds.dto.KitchenDisplayInfoDto;
import ru.sushi.delivery.kds.dto.OrderItemDto;
import ru.sushi.delivery.kds.dto.OrderShortDto;

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
    private final MealService mealService;
    private final IngredientService ingredientService;
    private final FlowCacheService flowCacheService; //todo remove
    private final ItemComboService itemComboService;

    public void createOrder(
            String name,
            List<Meal> meals,
            Instant shouldBeFinishedAt,
            Instant kitchenShouldGetOrderAt
    ) {
        this.orderService.createOrder(name, meals, shouldBeFinishedAt, kitchenShouldGetOrderAt);
    }

    public List<Meal> getAllMeals() {
        return this.mealService.getAllMeals();
    }

    public List<ItemCombo> getAllCombos() {
        return this.itemComboService.findAll();
    }

    public List<Meal> getAllExtras() {
        return this.mealService.getAllExtras();
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

    public void addItemToOrder(Long orderId, Meal meal) {
        this.orderService.createOrderItem(orderId, meal);
    }

    public void cancelOrder(Long orderId) {
        this.orderService.cancelOrder(orderId);
    }

    private OrderItemDto buildOrderItemDto(OrderItem item) {
        return OrderItemDto.builder()
                .id(item.getId())
                .orderName(item.getOrder().getName())
                .orderId(item.getOrder().getId())
                .name(item.getMeal().getName())
                .ingredients(new ArrayList<>(this.ingredientService.getMealIngredients(item.getMeal().getId())))
                .status(item.getStatus())
                .statusUpdatedAt(item.getStatusUpdatedAt())
                .createdAt(item.getStatusUpdatedAt())
                .timeToCook(180)
                //todo remove
                .currentStation(
                        this.flowCacheService.getStep(item.getMeal().getFlow().getId(), item.getCurrentFlowStep())
                                .getStation()
                )
                //todo remove
                .flowStepType(
                        this.flowCacheService.getStep(item.getMeal().getFlow().getId(), item.getCurrentFlowStep())
                                .getStepType()
                )
                .extra(item.getMeal().getProductType().isExtra())
                .build();
    }
}
