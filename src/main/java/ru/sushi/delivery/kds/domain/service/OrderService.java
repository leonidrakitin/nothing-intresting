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
import ru.sushi.delivery.kds.model.FlowStepType;
import ru.sushi.delivery.kds.model.OrderItemStationStatus;
import ru.sushi.delivery.kds.model.OrderStatus;

import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final FlowCacheService flowCacheService;

    public void createOrder(String name, List<Item> items) {
        Order order = orderRepository.save(Order.of(name));
        List<OrderItem> orderItems = items.stream()
                .map(item -> OrderItem.of(order, item))
                .toList();
        orderItemRepository.saveAll(orderItems);
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
        };
        if (orderItem.getStatus() == OrderItemStationStatus.COMPLETED) {
            FlowStep flowStep = this.flowCacheService.getNextStep(
                    orderItem.getItem().getFlow().getId(),
                    orderItem.getCurrentFlowStep()
            );
            if (flowStep.getStepType() != FlowStepType.FINAL_STEP) {
                orderItem = orderItem.toBuilder()
                        .status(OrderItemStationStatus.ADDED)
                        .currentFlowStep(flowStep.getStepOrder())
                        .screenChangedAt(Instant.now())
                        .build();

                    Station station = flowStep.getStation();
                //add notification to all station displays
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
            } else {
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
