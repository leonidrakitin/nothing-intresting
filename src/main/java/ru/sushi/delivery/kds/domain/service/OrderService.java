package ru.sushi.delivery.kds.domain.service;

import com.vaadin.flow.router.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.persist.entity.Order;
import ru.sushi.delivery.kds.domain.persist.entity.OrderItem;
import ru.sushi.delivery.kds.domain.persist.entity.flow.FlowStep;
import ru.sushi.delivery.kds.domain.persist.entity.flow.Screen;
import ru.sushi.delivery.kds.domain.persist.entity.flow.Station;
import ru.sushi.delivery.kds.domain.persist.entity.product.MenuItem;
import ru.sushi.delivery.kds.domain.persist.repository.OrderItemRepository;
import ru.sushi.delivery.kds.domain.persist.repository.OrderRepository;
import ru.sushi.delivery.kds.domain.persist.repository.flow.ScreenRepository;
import ru.sushi.delivery.kds.dto.OrderFullDto;
import ru.sushi.delivery.kds.dto.OrderItemDto;
import ru.sushi.delivery.kds.dto.PackageDto;
import ru.sushi.delivery.kds.model.FlowStepType;
import ru.sushi.delivery.kds.model.OrderItemStationStatus;
import ru.sushi.delivery.kds.model.OrderStatus;
import ru.sushi.delivery.kds.service.dto.BroadcastMessage;
import ru.sushi.delivery.kds.service.dto.BroadcastMessageType;
import ru.sushi.delivery.kds.service.listeners.CashListener;
import ru.sushi.delivery.kds.service.listeners.OrderChangesListener;
import ru.sushi.delivery.kds.websocket.WSMessageSender;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Log4j2
@Service
@RequiredArgsConstructor
public class OrderService {

    private final CashListener cashListener;
    private final IngredientService ingredientService;
    private final FlowCacheService flowCacheService;
    private final OrderItemRepository orderItemRepository;
    private final OrderChangesListener orderChangesListener;
    private final OrderRepository orderRepository;
    private final RecipeService recipeService;
    private final ScreenRepository screenRepository; //todo to service
    private final ProductPackageService productPackageService;
    private final WSMessageSender wsMessageSender;

    public void calculateOrderBalance(Order order) {
        List<Long> orderMenuItemIds = order.getOrderItems().stream()
                .map(OrderItem::getMenuItem)
                .map(MenuItem::getId)
                .toList();
        this.recipeService.calculateMenuItemsBalance(orderMenuItemIds);
    }

    public void createOrder(String name, List<MenuItem> menuItems) {
        Order order = this.orderRepository.save(Order.of(name));
        List<OrderItem> orderItems = new ArrayList<>();
        Set<FlowStep> flowSteps = new HashSet<>();
        for (MenuItem menuItem : menuItems) {
            OrderItem orderItem = OrderItem.of(order, menuItem);
            orderItems.add(orderItem);
            flowSteps.add(this.flowCacheService.getStep(
                orderItem.getMenuItem().getFlow().getId(),
                orderItem.getCurrentFlowStep()
            ));
        }
        this.orderItemRepository.saveAll(orderItems);
        List<PackageDto> packageDtos = this.productPackageService.calculatePackages(order);
        //todo notification somehow
        notificateAllScreens(flowSteps);
    }

    public List<OrderItem> getAllItemsByStationId(Long stationId) {
        List<OrderItem> orderItems = this.orderItemRepository.findAllItemsByStationId(stationId);

        List<OrderItem> firstList = orderItems.stream()
                .filter(oi -> oi.getStatus() == OrderItemStationStatus.STARTED)
                .sorted(Comparator.comparing(oi -> oi.getOrder().getStatusUpdateAt()))
                .toList();

        firstList = new ArrayList<>(firstList); // Чтобы можно было изменять список
        List<OrderItem> secondList = orderItems.stream()
                .filter(oi -> oi.getStatus() != OrderItemStationStatus.STARTED)
                .sorted(Comparator
                        .comparing((OrderItem oi) -> oi.getOrder().getCreatedAt()) // Сначала по createdAt
                        .thenComparing(oi -> oi.getMenuItem().getProductType().getId()) // Потом по productType.id
                )
                .toList();

        firstList.addAll(secondList);
        return firstList;
    }

    @Transactional
    public void updateAllOrderItemsToDone(Long orderId) {
        List<OrderItem> orderItems = new ArrayList<>();
        Integer doneStepOrder = null;
        for (OrderItem orderItem : this.getOrderItems(orderId)) {
            FlowStep step = this.flowCacheService.getStep(
                    orderItem.getMenuItem().getFlow().getId(),
                    orderItem.getCurrentFlowStep()
            );

            if (doneStepOrder == null) {
                doneStepOrder = this.flowCacheService.getDoneStep(orderItem.getMenuItem().getFlow().getId()).getStepOrder();
            }

            if (step.getStepType() != FlowStepType.FINAL_STEP) {
                orderItem = orderItem.toBuilder()
                        .currentFlowStep(doneStepOrder)
                        .status(OrderItemStationStatus.COMPLETED)
                        .build();
                orderItems.add(orderItem);
            }
        }
        this.orderItemRepository.saveAll(orderItems);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Not found order " + orderId));
        order = order.toBuilder().status(OrderStatus.READY).build();
        this.orderRepository.save(order);

        this.wsMessageSender.sendRefreshAll();
    }

    @Transactional
    public void updateOrderItem(Long orderItemId) {
        OrderItem orderItem = this.orderItemRepository.findById(orderItemId)
            .orElseThrow(() -> new IllegalArgumentException("Order item not found"));
        orderItem = switch (orderItem.getStatus()) {
            case ADDED -> orderItem.toBuilder()
                .status(OrderItemStationStatus.STARTED)
                .statusUpdatedAt(Instant.now())
                .build();
            case STARTED -> orderItem.toBuilder()
                .status(OrderItemStationStatus.COMPLETED)
                .statusUpdatedAt(Instant.now())
                .build();
            case COMPLETED -> orderItem;

            case CANCELED -> orderItem;
        };
        if (orderItem.getStatus() == OrderItemStationStatus.COMPLETED) {
            FlowStep nextFlowStep = this.flowCacheService.getNextStep(
                orderItem.getMenuItem().getFlow().getId(),
                orderItem.getCurrentFlowStep()
            );
            orderItem = orderItem.toBuilder()
                .status(OrderItemStationStatus.ADDED)
                .currentFlowStep(nextFlowStep.getStepOrder())
                .stationChangedAt(Instant.now())
                .build();

            if (nextFlowStep.getStepType() != FlowStepType.FINAL_STEP) {
                this.orderChangesListener.broadcast(
                        nextFlowStep.getStation().getId(),
                        BroadcastMessage.of(BroadcastMessageType.NOTIFICATION, "Новые позиции")
                );
            }
            else {
                this.cashListener.broadcast(BroadcastMessage.of(
                    BroadcastMessageType.NOTIFICATION,
                    orderItem.getOrder().getId() + " заказ обновлен"
                ));
            }
        }
        this.orderItemRepository.save(orderItem);
        this.updateOrderStatus(orderItem.getOrder());

        this.wsMessageSender.sendRefreshAll();
    }

    @Transactional
    public void updateOrderStatus(Order order) {

        if (OrderStatus.READY == order.getStatus()) {
            return;
        }

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
        int minimumStatus = this.definePriorityByOrderStatus(OrderStatus.READY);

        for (OrderItem orderItem : orderItems) {
            Station currentStation = this.getStationFromOrderItem(orderItem);
            int currentPriorityStatus = this.definePriorityByOrderStatus(currentStation.getOrderStatusAtStation());
            if (currentPriorityStatus > 0 && minimumStatus == 0) {
                minimumStatus = 1;
            }
            else {
                minimumStatus = Math.min(minimumStatus, currentPriorityStatus);
            }
        }

        OrderStatus newOrderStatus = this.defineOrderStatusByPriority(minimumStatus);

        if (newOrderStatus != order.getStatus()) {
            orderRepository.save(order.toBuilder()
                .status(newOrderStatus)
                .build()
            );
        }
    }

    public List<OrderFullDto> getAllActiveOrdersWithItems() {
        return orderRepository.findAllActive().stream()
                .map(order -> OrderFullDto.of(order, this.getOrderItemData(order)))
                .toList();
    }

    private List<OrderItemDto> getOrderItemData(Order order) {
        return order.getOrderItems().stream()
                .map(orderItem -> OrderItemDto.builder()
                        .id(orderItem.getId())
                        .orderId(order.getId())
                        .orderName(order.getName())
                        .name(orderItem.getMenuItem().getName())
                        .ingredients(this.ingredientService.getMenuItemIngredients(orderItem.getMenuItem().getId()))
                        .status(orderItem.getStatus())
                        .currentStation(this.getStationFromOrderItem(orderItem))
                        .flowStepType(this.getStepTypeFromOrderItem(orderItem))
                        .createdAt(orderItem.getStatusUpdatedAt())
                        .build()
                )
                .toList();
    }

    private Station getStationFromOrderItem(OrderItem orderItem) {
        return this.flowCacheService.getStep(
                        orderItem.getMenuItem().getFlow().getId(),
                        orderItem.getCurrentFlowStep()
                )
                .getStation();
    }

    private FlowStepType getStepTypeFromOrderItem(OrderItem orderItem) {
        return this.flowCacheService.getStep(
                        orderItem.getMenuItem().getFlow().getId(),
                        orderItem.getCurrentFlowStep()
                )
                .getStepType();
    }

    public void cancelOrderItem(Long orderId) {
        this.orderItemRepository.findById(orderId)
                .map(orderItem -> orderItem.toBuilder()
                        .currentFlowStep(FlowCacheService.CANCEL_STEP_ORDER)
                        .status(OrderItemStationStatus.CANCELED)
                        .build()
                )
                .ifPresent(orderItemRepository::save);
    }

    public void createOrderItem(Long orderId, MenuItem menuItem) {
        orderItemRepository.save(
                OrderItem.builder()
                        .order(Order.of(orderId))
                        .menuItem(menuItem)
                        .build()
        );
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Not found order " + orderId));
        orderRepository.save(
                order.toBuilder()
                        .status(OrderStatus.CANCELED)
                        .build()
        );
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        this.orderItemRepository.saveAll(
                orderItems.stream()
                        .map(orderItem -> orderItem.toBuilder()
                                .currentFlowStep(FlowCacheService.CANCEL_STEP_ORDER)
                                .status(OrderItemStationStatus.CANCELED)
                                .build())
                        .toList()
        );
    }

    public List<OrderItem> getOrderItems(Long orderId){
        return orderItemRepository.findByOrderId(orderId);
    }

    private int definePriorityByOrderStatus(OrderStatus orderStatus) {
        return switch (orderStatus) {
            case CREATED -> 0;
            case COOKING -> 1;
            case COLLECTING -> 2;
            case READY -> 3;
            case CANCELED -> 4;
        };
    }

    private OrderStatus defineOrderStatusByPriority(int priority) {
        return switch (priority) {
            case 0 -> OrderStatus.CREATED;
            case 1 -> OrderStatus.COOKING;
            case 2 -> OrderStatus.COLLECTING;
            case 3 -> OrderStatus.READY;
            default -> throw new IllegalStateException("Unexpected value: " + priority);
        };
    }

    private void notificateAllScreens(Set<FlowStep> flowSteps) {
        flowSteps.forEach(flowStep -> {
            this.orderChangesListener.broadcast(
                    flowStep.getStation().getId(),
                    BroadcastMessage.of(BroadcastMessageType.NOTIFICATION, "Новый заказ")
            );

            Screen screen = this.screenRepository.findByStationId(flowStep.getStation().getId());
            this.wsMessageSender.sendNotification(screen.getId(), "Новый заказ!");
        });
        this.wsMessageSender.sendRefreshAll();
    }
}
