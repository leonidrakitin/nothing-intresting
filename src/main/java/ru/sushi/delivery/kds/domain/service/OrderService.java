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
import ru.sushi.delivery.kds.dto.IngredientCompactDTO;
import ru.sushi.delivery.kds.dto.OrderItemDto;
import ru.sushi.delivery.kds.dto.OrderShortDto;
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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

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

    public void createOrder(
            String name,
            List<MenuItem> menuItems,
            Instant shouldBeFinishedAt,
            Instant kitchenShouldGetOrderAt
    ) {
        Order order = this.orderRepository.save(Order.of(name, shouldBeFinishedAt, kitchenShouldGetOrderAt));
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
    }

    public List<OrderShortDto> getAllItemsByStationId(Screen screen) {
        Long screenId = screen.getId();
        Long stationId = screen.getStation().getId();
        
        // Получаем заказы один раз
        List<Order> orders = orderRepository.findAllByStationId(stationId);
        
        // Собираем все orderItemDto из всех заказов
        List<OrderItemDto> allOrderItems = orders.stream()
                .flatMap(order -> this.getOrderFullItemData(order).stream()
                        .map(orderItemDto -> orderItemDto.toBuilder()
                                .ingredients(
                                        orderItemDto.getIngredients().stream()
                                                .filter(ingredient -> ingredient.getStationId().equals(screenId))
                                                .toList()
                                )
                                .build()
                        )
                )
                .limit(16)
                .toList();
        
        // Группируем orderItemDto по заказам
        Map<Long, List<OrderItemDto>> orderItemsByOrderId = allOrderItems.stream()
                .collect(Collectors.groupingBy(OrderItemDto::getOrderId));
        
        // Создаем OrderShortDto для каждого заказа
        return orders.stream()
                .filter(order -> orderItemsByOrderId.containsKey(order.getId()))
                .map(order -> OrderShortDto.of(order, orderItemsByOrderId.get(order.getId())))
                .toList();
    }

    @Transactional
    public void updateAllOrderItemsToDone(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Not found order " + orderId));
        order = order.toBuilder().status(OrderStatus.READY).build();
        this.orderRepository.save(order);

//        List<OrderItem> orderItems = new ArrayList<>();
//        Integer doneStepOrder = null;
//        for (OrderItem orderItem : this.getOrderItems(orderId)) {
//            FlowStep step = this.flowCacheService.getStep(
//                    orderItem.getMenuItem().getFlow().getId(),
//                    orderItem.getCurrentFlowStep()
//            );
//
//            if (doneStepOrder == null) {
//                doneStepOrder = this.flowCacheService.getDoneStep(orderItem.getMenuItem().getFlow().getId()).getStepOrder();
//            }
//
//            if (step.getStepType() != FlowStepType.FINAL_STEP) {
//                orderItem = orderItem.toBuilder()
//                        .currentFlowStep(doneStepOrder)
//                        .status(OrderItemStationStatus.COMPLETED)
//                        .build();
//                orderItems.add(orderItem);
//            }
//        }
//        this.orderItemRepository.saveAll(orderItems);

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
        this.orderItemRepository.saveAndFlush(orderItem);
        this.updateOrderStatus(orderItem.getOrder());

        this.wsMessageSender.sendRefreshAll();
    }

    @Transactional
    public void updateOrderStatus(Order order) {

        if (OrderStatus.READY == order.getStatus()) {
            return;
        }

        // Используем уже загруженные orderItems из order, если они есть
        List<OrderItem> orderItems = order.getOrderItems().isEmpty() 
            ? orderItemRepository.findByOrderId(order.getId())
            : order.getOrderItems();
            
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

    public List<OrderShortDto> getAllActiveOrdersWithItems() {
        return orderRepository.findAllActive().stream()
                .map(order -> OrderShortDto.of(order, this.getOrderFullItemData(order)))
                .toList();
    }

    public List<OrderShortDto> getAllActiveCollectorOrdersWithItems() {
        return orderRepository.findAllActive().stream()
                .map(order -> OrderShortDto.of(order, this.getOrderFullCollectorItemData(order)))
                .toList();
    }

    public List<OrderShortDto> getAllOrdersWithItemsBetweenDates(Instant from, Instant to) {
        return orderRepository.findAllBetweenDates(from, to).stream()
                .map(order -> OrderShortDto.of(order, this.getOrderFullItemData(order)))
                .toList()
                .reversed();
    }

    public List<OrderShortDto> getAllKitchenOrdersWithItems() {
        return orderRepository.findAllActiveKitchen().stream()
                .map(order -> OrderShortDto.of(order, this.getOrderShortItemData(order)))
                .toList();
    }

    private List<OrderItemDto> getOrderShortItemData(Order order) {
        return order.getOrderItems()
                .stream()
                .sorted(Comparator.<OrderItem>comparingInt(item -> item.getMenuItem().getProductType().getPriority())
                        .thenComparingLong(OrderItem::getId))
                .map(orderItem -> OrderItemDto.builder()
                        .id(orderItem.getId())
                        .orderId(order.getId())
                        .orderName(order.getName())
                        .name(orderItem.getMenuItem().getName())
                        .status(orderItem.getStatus())
                        .currentStation(this.getStationFromOrderItem(orderItem))
                        .flowStepType(this.getStepTypeFromOrderItem(orderItem))
                        .statusUpdatedAt(orderItem.getStatusUpdatedAt())
                        .createdAt(orderItem.getStatusUpdatedAt())
                        .extra(orderItem.getMenuItem().getProductType().isExtra())
                        .build()
                )
                .toList();
    }

    private List<OrderItemDto> getOrderFullItemData(Order order) {
        return order.getOrderItems()
                .stream()
                .sorted(Comparator.<OrderItem>comparingInt(item -> item.getMenuItem().getProductType().getPriority())
                        .thenComparingLong(OrderItem::getId))
                .map(orderItem -> {
                    List<IngredientCompactDTO> ingredients =
                            this.ingredientService.getMenuItemIngredients(orderItem.getMenuItem().getId());
                    AtomicReference<Double> sum = new AtomicReference<>((double) 0);
                    ingredients.stream()
                            .map(IngredientCompactDTO::getAmount)
                            .forEach(amount -> sum.updateAndGet(v -> v + amount));
                    return OrderItemDto.builder()
                                    .id(orderItem.getId())
                                    .orderId(order.getId())
                                    .orderName(order.getName())
                                    .name(orderItem.getMenuItem().getName() + " - " + sum + "г.")
                                    .ingredients(ingredients)
                                    .status(orderItem.getStatus())
                                    .currentStation(this.getStationFromOrderItem(orderItem))
                                    .flowStepType(this.getStepTypeFromOrderItem(orderItem))
                                    .statusUpdatedAt(orderItem.getStatusUpdatedAt())
                                    .createdAt(orderItem.getStatusUpdatedAt())
                                    .extra(orderItem.getMenuItem().getProductType().isExtra())
                                    .build();
                        }
                )
                .toList();
    }

    private List<OrderItemDto> getOrderFullCollectorItemData(Order order) {
        return order.getOrderItems()
                .stream()
                .sorted(Comparator.<OrderItem>comparingInt(item -> item.getMenuItem().getProductType().getPriority())
                        .thenComparingLong(OrderItem::getId))
                .map(orderItem -> OrderItemDto.builder()
                        .id(orderItem.getId())
                        .orderId(order.getId())
                        .orderName(order.getName())
                        .name(orderItem.getMenuItem().getName())
                        .ingredients(List.of())
                        .status(orderItem.getStatus())
                        .currentStation(this.getStationFromOrderItem(orderItem))
                        .flowStepType(this.getStepTypeFromOrderItem(orderItem))
                        .statusUpdatedAt(orderItem.getStatusUpdatedAt())
                        .createdAt(orderItem.getStatusUpdatedAt())
                        .extra(orderItem.getMenuItem().getProductType().isExtra())
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
        this.wsMessageSender.sendRefreshAll();
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
        this.wsMessageSender.sendRefreshAll();
    }

    public List<OrderItem> getOrderItems(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    @Transactional
    public void updateKitchenShouldGetOrderAt(Long orderId, Instant newInstant) {
        this.orderRepository.save(
                this.orderRepository.findById(orderId)
                        .map(order -> order.toBuilder().kitchenShouldGetOrderAt(newInstant).build())
                        .orElseThrow(() -> new NotFoundException("Not found order " + orderId))
        );
    }

    @Transactional
    public void checkAndUpdateKitchenGotOrderAt(Instant now) {
        Set<FlowStep> flowSteps = new HashSet<>();
        orderRepository.findAllNotStarted()
                .stream()
                .filter(order ->
                        order.getKitchenShouldGetOrderAt() == null
                        || order.getKitchenShouldGetOrderAt().isBefore(now)
                )
                .forEach(order -> {
                    order.setKitchenGotOrderAt(now);
                    for (OrderItem orderItem : order.getOrderItems()) {
                        FlowStep step = this.flowCacheService.getStep(
                                orderItem.getMenuItem().getFlow().getId(),
                                orderItem.getCurrentFlowStep()
                        );
                        flowSteps.add(step);
                    }
                });
        notificateAllScreens(flowSteps);
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
