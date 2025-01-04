package ru.sushi.delivery.kds.domain.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.persist.entity.FlowStep;
import ru.sushi.delivery.kds.domain.persist.entity.Item;
import ru.sushi.delivery.kds.domain.persist.entity.Order;
import ru.sushi.delivery.kds.domain.persist.entity.OrderItem;
import ru.sushi.delivery.kds.domain.persist.entity.Station;
import ru.sushi.delivery.kds.domain.persist.repository.OrderItemRepository;
import ru.sushi.delivery.kds.domain.persist.repository.OrderRepository;
import ru.sushi.delivery.kds.domain.persist.repository.StationRepository;
import ru.sushi.delivery.kds.model.FlowStepType;
import ru.sushi.delivery.kds.model.OrderItemStationStatus;
import ru.sushi.delivery.kds.model.OrderStatus;
import ru.sushi.delivery.kds.service.dto.BroadcastMessage;
import ru.sushi.delivery.kds.service.dto.BroadcastMessageType;
import ru.sushi.delivery.kds.service.listeners.CashListener;
import ru.sushi.delivery.kds.service.listeners.OrderChangesListener;

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
    private final StationRepository stationRepository;

    public void createOrder(String name, List<Item> items) {
        Order order = orderRepository.save(Order.of(name));

        List<OrderItem> orderItems = new ArrayList<>();
        Set<FlowStep> flowSteps = new HashSet<>();
        for (Item item : items) {
            OrderItem orderItem = OrderItem.of(order, item);
            orderItems.add(orderItem);
            flowSteps.add(this.flowCacheService.getCurrentStep(
                orderItem.getItem().getFlow().getId(),
                orderItem.getCurrentFlowStep()
            ));
        }
        orderItemRepository.saveAll(orderItems);
        flowSteps.forEach(flowStep -> this.orderChangesListener.broadcast(
            flowStep.getStation().getId(),
            BroadcastMessage.of(BroadcastMessageType.NOTIFICATION, "Новый заказ")
        ));
    }

    public List<OrderItem> getAllItemsByStationId(Long stationId) {
        return this.orderItemRepository.findAllItemsByStationId(stationId);
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
            FlowStep flowStep = this.flowCacheService.getNextStep(
                orderItem.getItem().getFlow().getId(),
                orderItem.getCurrentFlowStep()
            );
            orderItem = orderItem.toBuilder()
                .status(OrderItemStationStatus.ADDED)
                .currentFlowStep(flowStep.getStepOrder())
                .stationChangedAt(Instant.now())
                .build();

            if (flowStep.getStepType() != FlowStepType.FINAL_STEP) {
                this.orderChangesListener.broadcast(
                    flowStep.getStation().getId(),
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
    }

    @Transactional
    public void updateOrderStatus(Order order) {

        if (OrderStatus.READY == order.getStatus()) {
            return;
        }

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
        int minimumStatus = 1;

        for (OrderItem orderItem : orderItems) {
            Station currentStation = this.flowCacheService.getCurrentStep(
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

    public List<Order> getAllOrdersWithItems() {
        return orderRepository.findAllWithItems();
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElseThrow();
    }

    public OrderItem getOrderItemById(Long id) {
        return orderItemRepository.findById(id).orElseThrow();
    }

    public void removeItemFromOrder(Long id) {
        OrderItem orderItem = this.getOrderItemById(id);
        Station station = stationRepository.findByOrderStatusAtStation(OrderStatus.CANCELED);
        Integer currentFlowStep = flowCacheService.getStepByStationAndFlowId(
            station.getId(), orderItem.getItem().getFlow().getId()
        ).getId();
        OrderItem updatedOrderItem = orderItem.toBuilder()
            .currentFlowStep(currentFlowStep)
            .status(OrderItemStationStatus.CANCELED)
            .build();

        orderItemRepository.save(updatedOrderItem);
    }

    public void addItemToOrder(Long orderId, Item item) {
        OrderItem orderItem = OrderItem.builder()
            .order(this.getOrderById(orderId))
            .item(item)
            .build();
        orderItemRepository.save(orderItem);
    }

    @Transactional
    public void cancelOrder(Long orderId){
        Order order = orderRepository.getOrderById(orderId);
        Order updatedOrder = order.toBuilder().status(OrderStatus.CANCELED).build();
        for (OrderItem orderItem:order.getOrderItems()){
            this.removeItemFromOrder(orderItem.getId());
        }
        orderRepository.save(updatedOrder);
    }
}
