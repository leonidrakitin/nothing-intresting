package ru.sushi.delivery.kds.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sushi.delivery.kds.domain.persist.entity.Order;
import ru.sushi.delivery.kds.domain.persist.entity.OrderItem;
import ru.sushi.delivery.kds.domain.persist.entity.flow.Station;
import ru.sushi.delivery.kds.domain.persist.repository.OrderItemRepository;
import ru.sushi.delivery.kds.domain.persist.repository.OrderRepository;
import ru.sushi.delivery.kds.model.OrderItemStationStatus;
import ru.sushi.delivery.kds.model.OrderStatus;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class AsyncOrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final FlowCacheService flowCacheService;

    @Async("taskExecutor")
    @Transactional
    public void updateOrderStatus(Order order) {
        log.info("Starting async updateOrderStatus for order: {}", order.getId());
        
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
        updateOrderStatusWithItems(order, orderItems);
        
        log.info("Completed async updateOrderStatus for order: {}", order.getId());
    }

    @Scheduled(fixedRate = 20000)
    @Transactional
    public void updateAllActiveOrderStatuses() {
        log.info("Starting scheduled task to update all active order statuses");
        
        List<Order> activeOrders = orderRepository.findAllActive();
        log.info("Found {} active orders to update", activeOrders.size());
        
        if (activeOrders.isEmpty()) {
            return;
        }
        
        // Fetch order IDs
        List<Long> orderIds = activeOrders.stream()
                .map(Order::getId)
                .collect(Collectors.toList());
        
        // Fetch all order items in bulk
        List<OrderItem> allOrderItems = orderItemRepository.findByOrderIdIn(orderIds);
        
        // Group order items by order ID
        Map<Long, List<OrderItem>> orderItemsMap = allOrderItems.stream()
                .collect(Collectors.groupingBy(item -> item.getOrder().getId()));
        
        log.info("Fetched {} order items for {} orders", allOrderItems.size(), activeOrders.size());
        
        // Process each order with pre-fetched items
        for (Order order : activeOrders) {
            try {
                List<OrderItem> orderItems = orderItemsMap.getOrDefault(order.getId(), List.of());
                updateOrderStatusWithItems(order, orderItems);
            } catch (Exception e) {
                log.error("Error updating order {} status: {}", order.getId(), e.getMessage(), e);
            }
        }
        
        log.info("Completed scheduled task to update all active order statuses");
    }

    @Transactional
    public void updateOrderStatusSync(Order order) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
        updateOrderStatusWithItems(order, orderItems);
    }

    private void updateOrderStatusWithItems(Order order, List<OrderItem> orderItems) {
        if (OrderStatus.READY == order.getStatus()) {
            log.debug("Order {} is already READY, skipping status update", order.getId());
            return;
        }

        // Инициализируем минимальный приоритет максимальным значением
        int minimumStatus = 4;
//        boolean hasAnyStatus = false;

        for (OrderItem orderItem : orderItems) {
            Station currentStation = this.getStationFromOrderItem(orderItem);
            int currentPriorityStatus = this.definePriorityByOrderStatus(currentStation.getOrderStatusAtStation());
            if (
                currentPriorityStatus == 0
                && orderItem.getStatus() == OrderItemStationStatus.ADDED
                && currentStation.getId() == 2
            ) {
                currentPriorityStatus = 1;
            }
            // Учитываем только не-READY статусы
            if (currentPriorityStatus < 4) {
//                hasAnyStatus = true;
                minimumStatus = Math.min(minimumStatus, currentPriorityStatus);
            }
        }

        // Если все элементы в READY, оставляем READY
        OrderStatus newOrderStatus = this.defineOrderStatusByPriority(minimumStatus);

        if (newOrderStatus != order.getStatus()) {
            orderRepository.save(order.toBuilder()
                    .status(newOrderStatus)
                    .build()
            );
            log.debug("Updated order {} status from {} to {}", order.getId(), order.getStatus(), newOrderStatus);
        }
    }

    private Station getStationFromOrderItem(OrderItem orderItem) {
        return this.flowCacheService.getStep(
                orderItem.getMenuItem().getFlow().getId(),
                orderItem.getCurrentFlowStep()
        ).getStation();
    }

    private int definePriorityByOrderStatus(OrderStatus orderStatus) {
        return switch (orderStatus) {
            case COOKING -> 0;
            case CREATED -> 1;
            case COLLECTING -> 2;
            case CANCELED -> 3;
            default -> 4;
        };
    }

    private OrderStatus defineOrderStatusByPriority(int priority) {
        return switch (priority) {
            case 0 -> OrderStatus.COOKING;
            case 1 -> OrderStatus.CREATED;
            case 2 -> OrderStatus.COLLECTING;
            case 3 -> OrderStatus.CANCELED;
            default -> OrderStatus.READY;
        };
    }
}
