package ru.sushi.delivery.kds.domain.service;

import com.vaadin.flow.router.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.persist.entity.FlowStep;
import ru.sushi.delivery.kds.domain.persist.entity.Item;
import ru.sushi.delivery.kds.domain.persist.entity.Order;
import ru.sushi.delivery.kds.domain.persist.entity.OrderItem;
import ru.sushi.delivery.kds.domain.persist.entity.Screen;
import ru.sushi.delivery.kds.domain.persist.entity.Station;
import ru.sushi.delivery.kds.domain.persist.repository.OrderItemRepository;
import ru.sushi.delivery.kds.domain.persist.repository.OrderRepository;
import ru.sushi.delivery.kds.domain.persist.repository.ScreenRepository;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final FlowCacheService flowCacheService;
    private final OrderChangesListener orderChangesListener;
    private final CashListener cashListener;
    private final ScreenRepository screenRepository;
    private final WSMessageSender wsMessageSender;

    public void createOrder(String name, List<Item> items) {
        Order order = orderRepository.save(Order.of(name));

        List<OrderItem> orderItems = new ArrayList<>();
        Set<FlowStep> flowSteps = new HashSet<>();
        for (Item item : items) {
            OrderItem orderItem = OrderItem.of(order, item);
            orderItems.add(orderItem);
            flowSteps.add(this.flowCacheService.getStep(
                orderItem.getItem().getFlow().getId(),
                orderItem.getCurrentFlowStep()
            ));
        }
        orderItemRepository.saveAll(orderItems);
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

    public List<OrderItem> getAllItemsByStationId(Long stationId) {
        return this.orderItemRepository.findAllItemsByStationId(stationId);
    }

    @Transactional
    public void updateAllOrderItemsToDone(Long orderId) {
        List<OrderItem> orderItems = new ArrayList<>();
        Integer doneStepOrder = null;
        for (OrderItem orderItem : this.getOrderItems(orderId)) {
            FlowStep step = this.flowCacheService.getStep(
                orderItem.getItem().getFlow().getId(),
                orderItem.getCurrentFlowStep()
            );

            if (doneStepOrder == null) {
                doneStepOrder = this.flowCacheService.getDoneStep(orderItem.getItem().getFlow().getId()).getStepOrder();
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
                orderItem.getItem().getFlow().getId(),
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
            Station currentStation = this.flowCacheService.getStep(
                    orderItem.getItem().getFlow().getId(),
                    orderItem.getCurrentFlowStep()
                )
                .getStation();
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

    public List<Order> getAllOrdersWithItems() {
        return orderRepository.findAllActive();
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

    public void createOrderItem(Long orderId, Item item) {
        orderItemRepository.save(
            OrderItem.builder()
                .order(Order.of(orderId))
                .item(item)
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

    public List<OrderItem> getOrderItems(Long orderId) {
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
}
