package ru.sushi.delivery.kds.service;

import lombok.Data;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.Item;
import ru.sushi.delivery.kds.persist.model.ScreenSettings;

import java.util.*;

@Service
@Data
public class ChefScreenService {

    private final ChefScreenHolder screenHolder = new ChefScreenHolder();

    public Long createNewScreen(long id) {
        return screenHolder.createNewSession(id);
    }

    public List<Item> getScreenOrders(Long screenId) {
        return screenHolder.getOrdersForScreen(screenId);
    }

    public ScreenSettings getScreenSettings(Long screenId) {
        return screenHolder.getScreenSettings(screenId);
    }

    // Добавить заказ в конкретный экран
    public void addOrder(Long screenId, Item order) {
        screenHolder.getOrdersForScreen(screenId).add(order);
    }

    // Удалить заказ
    public void removeOrder(long screenId, long orderId) {
        Item order = screenHolder.getScreenOrder(screenId, orderId);
        if (!order.getPlacesFlow().isEmpty()) {
            int next = order.getNextStep();
            order.setNextStep(next + 1);
            for (var displayId : order.getPlacesFlow().get(next).getDisplays()) {
                screenHolder.getOrdersForScreen(displayId).add(order);
            }
        }
        screenHolder.getOrdersForScreen(screenId).remove(order);
    }
}
