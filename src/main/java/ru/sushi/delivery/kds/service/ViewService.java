package ru.sushi.delivery.kds.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.persist.entity.Item;
import ru.sushi.delivery.kds.domain.persist.entity.Order;
import ru.sushi.delivery.kds.domain.persist.entity.OrderItem;
import ru.sushi.delivery.kds.domain.persist.entity.Screen;
import ru.sushi.delivery.kds.domain.persist.entity.Station;
import ru.sushi.delivery.kds.domain.service.FlowCacheService;
import ru.sushi.delivery.kds.domain.service.IngredientCacheService;
import ru.sushi.delivery.kds.domain.service.ItemService;
import ru.sushi.delivery.kds.domain.service.OrderService;
import ru.sushi.delivery.kds.domain.service.ScreenService;
import ru.sushi.delivery.kds.dto.IngredientDTO;
import ru.sushi.delivery.kds.dto.KitchenDisplayInfoDto;
import ru.sushi.delivery.kds.dto.OrderFullDto;
import ru.sushi.delivery.kds.dto.OrderItemDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ViewService {

    private final OrderService orderService;
    private final ScreenService screenService;
    private final ItemService itemService;
    private final IngredientCacheService ingredientCacheService;
    private final FlowCacheService flowCacheService;

    public void createOrder(String name, List<Item> items) {
        this.orderService.createOrder(name, items);
    }

    public List<Item> getAllMenuItems() {
        return this.itemService.getAllMenuItems();
    }

    public List<KitchenDisplayInfoDto> getAvailableDisplaysData() {
        List<KitchenDisplayInfoDto> kitchenDisplayData = new ArrayList<>();
        for (Screen screen : screenService.getAll()) {
            kitchenDisplayData.add(new KitchenDisplayInfoDto(screen.getId(), screen.getStation().getName()));
        }
        return kitchenDisplayData;
    }

    public Optional<Long> getScreenStationIfExists(Long screenId) {
        return screenService.get(screenId).map(Screen::getStation).map(Station::getId);
    }

    public List<OrderItemDto> getScreenOrderItems(Long screenId) {
        Screen screen = screenService.getOrThrow(screenId);
        return orderService.getAllItemsByStationId(screen.getStation().getId()).stream()
            .map(this::buildOrderItemDto)
            .toList();
    }

    public void updateStatus(Long orderItemId) {
        this.orderService.updateOrderItem(orderItemId);
    }

    public List<OrderFullDto> getAllOrdersWithItems() {
        return orderService.getAllOrdersWithItems().stream()
            .map(order -> OrderFullDto.builder()
                .id(order.getId())
                .name(order.getName())
                .status(order.getStatus())
                .items(getOrderItemData(order))
                .build()
            )
            .toList();
    }

    public List<OrderItemDto> getOrderItems(Long orderId) {
        return orderService.getOrderItems(orderId).stream()
            .map(this::buildOrderItemDto)
            .toList();
    }

    public void updateAllOrderItemsToDone(Long orderId) {
        this.orderService.updateAllOrderItemsToDone(orderId);
    }

    public void cancelOrderItem(Long orderItemId) {
        orderService.cancelOrderItem(orderItemId);
    }

    public void addItemToOrder(Long orderId, Item item) {
        orderService.createOrderItem(orderId, item);
    }

    public void cancelOrder(Long orderId){
        orderService.cancelOrder(orderId);
    }

    private List<OrderItemDto> getOrderItemData(Order order) {
        return order.getOrderItems().stream()
            .map(orderItem -> OrderItemDto.builder()
                .id(orderItem.getId())
                .orderId(order.getId())
                .name(orderItem.getItem().getName())
                .ingredients(
                    this.ingredientCacheService
                        .getItemIngredients(orderItem.getItem().getId())
                        .stream()
                        .map(ingredient -> IngredientDTO.builder()
                            .name(ingredient.getName())
                            .stationId(ingredient.getStationId())
                            .build()
                        )
                        .toList()
                )
                .status(orderItem.getStatus())
                .currentStation(this.flowCacheService.getStep(
                            orderItem.getItem().getFlow().getId(),
                            orderItem.getCurrentFlowStep()
                        )
                        .getStation()
                )
                .createdAt(orderItem.getStatusUpdatedAt())
                .build()
            )
            .toList();
    }

    private OrderItemDto buildOrderItemDto(OrderItem item) {
        return OrderItemDto.builder()
            .id(item.getId())
            .orderId(item.getOrder().getId())
            .name(item.getItem().getName())
            .ingredients(
                this.ingredientCacheService.getItemIngredients(item.getItem().getId()).stream()
                    .map(ingredient -> IngredientDTO.builder()
                        .name(ingredient.getName())
                        .stationId(ingredient.getStationId())
                        .build()
                    )
                    .toList()
            )
            .status(item.getStatus())
            .createdAt(item.getStatusUpdatedAt())
            .build();
    }
}
