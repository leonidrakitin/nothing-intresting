package ru.sushi.delivery.kds.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.persist.entity.Order;
import ru.sushi.delivery.kds.domain.persist.entity.OrderItem;
import ru.sushi.delivery.kds.domain.persist.entity.flow.Screen;
import ru.sushi.delivery.kds.domain.persist.entity.flow.Station;
import ru.sushi.delivery.kds.domain.persist.entity.product.Position;
import ru.sushi.delivery.kds.domain.service.FlowCacheService;
import ru.sushi.delivery.kds.domain.service.IngredientService;
import ru.sushi.delivery.kds.domain.service.OrderService;
import ru.sushi.delivery.kds.domain.service.PositionService;
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
    private final PositionService positionService;
    private final IngredientService ingredientService;
    private final FlowCacheService flowCacheService;

    public void createOrder(String name, List<Position> positions) {
        this.orderService.createOrder(name, positions);
    }

    public List<Position> getAllMenuItems() {
        return this.positionService.getAllMenuItems();
    }

    public List<KitchenDisplayInfoDto> getAvailableDisplaysData() {
        List<KitchenDisplayInfoDto> kitchenDisplayData = new ArrayList<>();
        for (Screen screen : screenService.getAll()) {
            kitchenDisplayData.add(new KitchenDisplayInfoDto(screen.getId(), screen.getStation().getName()));
        }
        return kitchenDisplayData;
    }

    public Optional<Long> getScreenStationIfExists(String screenId) {
        return screenService.get(screenId).map(Screen::getStation).map(Station::getId);
    }

    public List<OrderItemDto> getScreenOrderItems(String screenId) {
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

    public void addItemToOrder(Long orderId, Position position) {
        orderService.createOrderItem(orderId, position);
    }

    public void cancelOrder(Long orderId){
        orderService.cancelOrder(orderId);
    }

    private List<OrderItemDto> getOrderItemData(Order order) {
        return order.getOrderItems().stream()
            .map(orderItem -> OrderItemDto.builder()
                .id(orderItem.getId())
                .orderId(order.getId())
                .name(orderItem.getPosition().getName())
                .ingredients(
                    this.ingredientService
                        .getItemIngredients(orderItem.getPosition().getId())
                        .stream()
                        .map(ingredient -> IngredientDTO.builder()
                            .name(ingredient.getName())
//                            .stationId(ingredient.getStationId()) //TODO from recipe
                            .build()
                        )
                        .toList()
                )
                .status(orderItem.getStatus())
                .currentStation(this.flowCacheService.getStep(
                            orderItem.getPosition().getFlow().getId(),
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
            .name(item.getPosition().getName())
            .ingredients(
                this.ingredientService.getItemIngredients(item.getPosition().getId()).stream()
                    .map(ingredient -> IngredientDTO.builder()
                        .name(ingredient.getName())
//                        .stationId(ingredient.getStationId()) //todo from recipe
                        .build()
                    )
                    .toList()
            )
            .status(item.getStatus())
            .createdAt(item.getStatusUpdatedAt())
            .build();
    }
}
