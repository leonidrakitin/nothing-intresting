package ru.sushi.delivery.kds.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sushi.delivery.kds.domain.persist.entity.Order;
import ru.sushi.delivery.kds.domain.persist.entity.OrderItem;
import ru.sushi.delivery.kds.domain.persist.entity.flow.Station;
import ru.sushi.delivery.kds.domain.persist.repository.OrderItemRepository;
import ru.sushi.delivery.kds.domain.persist.repository.OrderRepository;
import ru.sushi.delivery.kds.model.OrderStatus;

import java.util.List;

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

        if (OrderStatus.READY == order.getStatus()) {
            log.debug("Order {} is already READY, skipping status update", order.getId());
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
            log.debug("Updated order {} status from {} to {}", order.getId(), order.getStatus(), newOrderStatus);
        } else {
            log.debug("Order {} status unchanged: {}", order.getId(), order.getStatus());
        }
        
        log.debug("Completed async updateOrderStatus for order: {}", order.getId());
    }

    private Station getStationFromOrderItem(OrderItem orderItem) {
        return this.flowCacheService.getStep(
                orderItem.getMenuItem().getFlow().getId(),
                orderItem.getCurrentFlowStep()
        ).getStation();
    }

    private int definePriorityByOrderStatus(OrderStatus orderStatus) {
        return switch (orderStatus) {
            case READY -> 0;
            case COLLECTING -> 1;
            case COOKING -> 2;
            case CREATED -> 3;
            case CANCELED -> 4;
        };
    }

    private OrderStatus defineOrderStatusByPriority(int priority) {
        return switch (priority) {
            case 0 -> OrderStatus.READY;
            case 1 -> OrderStatus.COLLECTING;
            case 2 -> OrderStatus.COOKING;
            case 3 -> OrderStatus.CREATED;
            default -> OrderStatus.CREATED;
        };
    }
}
