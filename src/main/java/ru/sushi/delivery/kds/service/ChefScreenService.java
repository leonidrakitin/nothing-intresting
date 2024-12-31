package ru.sushi.delivery.kds.service;

import lombok.Data;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.persist.model.OrderItem;
import ru.sushi.delivery.kds.persist.model.ScreenSettings;

import java.util.*;

@Service
@Data
public class ChefScreenService {

    private final ChefScreenHolder screenHolder = new ChefScreenHolder();

    public Long createNewScreen() {
        return screenHolder.createNewSession();
    }

    public List<OrderItem> getScreenOrders(Long screenId) {
        return screenHolder.getOrdersForScreen(screenId);
    }

    public ScreenSettings getScreenSettings(Long screenId) {
        return screenHolder.getScreenSettings(screenId);
    }

    // Добавить заказ в конкретный экран
    public void addOrder(Long screenId, OrderItem order) {
        screenHolder.getOrdersForScreen(screenId).add(order);
    }

    // Удалить заказ
    public void removeOrder(long screenId, long orderId) {
        OrderItem order = screenHolder.getScreenOrder(screenId, orderId);
        if (order.getNextScreen() != null) {
            screenHolder.getOrdersForScreen(order.getNextScreen())
                    .add(new OrderItem(order.getName(), order.getDetails()));
        }
        screenHolder.getOrdersForScreen(screenId).remove(order);
    }
}
