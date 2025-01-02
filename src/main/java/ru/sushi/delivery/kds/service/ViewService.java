package ru.sushi.delivery.kds.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.model.OrderStatus;
import ru.sushi.delivery.kds.domain.persist.entity.Ingredient;
import ru.sushi.delivery.kds.domain.persist.entity.Item;
import ru.sushi.delivery.kds.domain.persist.entity.Order;
import ru.sushi.delivery.kds.domain.persist.entity.OrderItem;
import ru.sushi.delivery.kds.domain.persist.entity.Screen;
import ru.sushi.delivery.kds.domain.persist.entity.Station;
import ru.sushi.delivery.kds.domain.service.OrderService;
import ru.sushi.delivery.kds.domain.service.ScreenService;
import ru.sushi.delivery.kds.domain.service.StationService;
import ru.sushi.delivery.kds.dto.KitchenDisplayInfoDto;
import ru.sushi.delivery.kds.dto.OrderItemDto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ViewService {

    private final OrderService orderService;
    private final ScreenService screenService;

    public void createOrder(List<Item> items) {
        this.orderService.createOrder(items);
    }
    public List<KitchenDisplayInfoDto> getAvailableDisplaysData() {
        List<KitchenDisplayInfoDto> kitchenDisplayData = new ArrayList<>();
        for (Screen screen : screenService.getAll()) {
            kitchenDisplayData.add(new KitchenDisplayInfoDto(screen.getId(), screen.getStation().getName()));
        }
        return kitchenDisplayData;
    }

    public boolean checkScreenExists(String screenId) {
        return screenService.get(screenId).isPresent();
    }

    public List<OrderItemDto> getScreenOrderItems(String screenId) {
        Screen screen = screenService.getOrThrow(screenId);
        return orderService.getOrderItems().stream()
                .filter(item -> item.getCurrentStation().equals(screen.getStation()))
                .map(item -> OrderItemDto.builder()
                        .id(item.getId())
                        .orderId(item.getOrderId())
                        .name(item.getItem().getName())
                        .ingredients(item.getItem().getIngredients().stream().map(Ingredient::toString).toList())
                        .status(item.getStatus())
                        .createdAt(item.getStatusUpdatedAt())
                        .build()
                )
                .toList();
    }

    public void updateStatus(Long orderItemId) {
        this.orderService.updateOrderItem(orderItemId);
    }
}
