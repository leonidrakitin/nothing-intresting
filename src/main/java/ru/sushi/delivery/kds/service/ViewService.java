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
    private final MenuItemService menuItemService;
    private final IngredientService ingredientService;
    private final FlowCacheService flowCacheService; //todo remove
    private final ItemComboService itemComboService;

    public void createOrder(String name, List<MenuItem> menuItems) {
        this.orderService.createOrder(name, menuItems);
    }

    public List<MenuItem> getAllMenuItems() {
        return this.menuItemService.getAllMenuItems();
    }

    public List<ItemCombo> getAllCombos() {
        return this.itemComboService.findAll();
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

    public List<OrderItemDto> getScreenOrderItems(Long screenId) {
        Screen screen = this.screenService.getOrThrow(screenId);
        List<OrderItemDto> orderItemsList = this.orderService.getAllItemsByStationId(screen.getStation().getId())
                .stream()
                .map(this::buildOrderItemDto)
                .toList();

        //todo move to service method, need separate it first
        orderItemsList.forEach(orderItemData -> orderItemData.getIngredients()
                .removeIf(ingredients -> !ingredients.getStationId().equals(screenId)));

        return orderItemsList;
    }

    public void updateStatus(Long orderItemId) {
        this.orderService.updateOrderItem(orderItemId);
    }

    public List<OrderFullDto> getAllOrdersWithItems() {
        return this.orderService.getAllActiveOrdersWithItems();
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

    public void cancelOrder(Long orderId){
        this.orderService.cancelOrder(orderId);
    }

    private OrderItemDto buildOrderItemDto(OrderItem item) {
        return OrderItemDto.builder()
                .id(item.getId())
                .orderId(item.getOrder().getId())
                .name(item.getMenuItem().getName())
                .ingredients(new ArrayList<>(this.ingredientService.getMenuItemIngredients(item.getMenuItem().getId())))
                .status(item.getStatus())
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
                .build();
    }
}
