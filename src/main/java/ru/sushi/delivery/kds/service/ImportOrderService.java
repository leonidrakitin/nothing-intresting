package ru.sushi.delivery.kds.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.persist.entity.ItemCombo;
import ru.sushi.delivery.kds.domain.persist.entity.product.MenuItem;
import ru.sushi.delivery.kds.dto.ImportOrderRequest;
import ru.sushi.delivery.kds.dto.ImportOrderResponse;
import ru.sushi.delivery.kds.dto.OrderAddressDto;
import ru.sushi.delivery.kds.dto.ParsedOrderDto;
import ru.sushi.delivery.kds.model.OrderType;
import ru.sushi.delivery.kds.model.PaymentType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Создание заказа из текста (импорт): парсинг тем же парсером, что и в UI, и сохранение через MultiCityOrderService.
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class ImportOrderService {

    private final OrderTextParserService orderTextParserService;
    private final MultiCityViewService multiCityViewService;
    private final MultiCityOrderService multiCityOrderService;

    /**
     * Создаёт заказ из текста: парсит текст и создаёт заказ в выбранном городе.
     *
     * @param request текст заказа и опционально city, orderNumber
     * @return созданный заказ (id, имя, город)
     * @throws IllegalArgumentException если город не определён, есть нераспознанные позиции или дубликат номера заказа
     */
    public ImportOrderResponse importOrder(ImportOrderRequest request) {
        MultiCityViewService.City viewCity = resolveCity(request);
        MultiCityOrderService.City orderCity = toOrderCity(viewCity);
        List<MenuItem> menuItems = multiCityViewService.getMenuItems(viewCity);
        List<ItemCombo> combos = multiCityViewService.getCombos(viewCity);
        List<MenuItem> extras = multiCityViewService.getExtras(viewCity);

        ParsedOrderDto parsed = orderTextParserService.parseOrderText(
                request.getText(), menuItems, combos);

        String orderName = resolveOrderNumber(request, parsed);
        if (orderName == null || orderName.isBlank()) {
            orderName = "AI-" + UUID.randomUUID().toString().substring(0, 8);
        }

        if (multiCityOrderService.orderExistsByNameToday(orderCity, orderName)) {
            throw new IllegalArgumentException("Заказ с номером «" + orderName + "» уже существует сегодня в городе " + orderCity);
        }

        List<String> unrecognized = new ArrayList<>();
        for (ParsedOrderDto.ParsedCombo c : parsed.getCombos()) {
            if (c.getCombo() == null) {
                unrecognized.add("Сет: " + c.getName() + " x" + c.getQuantity());
            }
        }
        for (ParsedOrderDto.ParsedItem i : parsed.getItems()) {
            if (i.getMenuItem() == null) {
                unrecognized.add(i.getName() + " x" + i.getQuantity());
            }
        }
        if (!unrecognized.isEmpty()) {
            throw new IllegalArgumentException(
                    "Нераспознанные позиции (нет в меню города): " + String.join(", ", unrecognized));
        }

        List<MenuItem> itemsToCreate = new ArrayList<>();

        for (ParsedOrderDto.ParsedCombo combo : parsed.getCombos()) {
            if (combo.getCombo() != null && combo.getCombo().getMenuItems() != null) {
                for (MenuItem item : combo.getCombo().getMenuItems()) {
                    for (int q = 0; q < combo.getQuantity(); q++) {
                        itemsToCreate.add(item);
                    }
                }
            }
        }
        for (ParsedOrderDto.ParsedItem item : parsed.getItems()) {
            if (item.getMenuItem() != null) {
                for (int q = 0; q < item.getQuantity(); q++) {
                    itemsToCreate.add(item.getMenuItem());
                }
            }
        }
        for (Map.Entry<String, Integer> extraEntry : parsed.getExtras().entrySet()) {
            MenuItem extraItem = extras.stream()
                    .filter(e -> Objects.equals(e.getName(), extraEntry.getKey()))
                    .findFirst()
                    .orElse(null);
            if (extraItem != null) {
                for (int q = 0; q < extraEntry.getValue(); q++) {
                    itemsToCreate.add(extraItem);
                }
            }
        }

        if (itemsToCreate.isEmpty()) {
            throw new IllegalArgumentException("В заказе нет ни одной распознанной позиции меню.");
        }

        Instant shouldBeFinishedAt = parsed.getFinishTime();
        Instant kitchenShouldGetOrderAt = parsed.getKitchenStartTime();
        OrderType orderType = parsed.getOrderType() != null ? parsed.getOrderType() : OrderType.PICKUP;
        OrderAddressDto address = parsed.getOrderType() == OrderType.DELIVERY ? parsed.getAddress() : null;
        String customerPhone = parsed.getCustomerPhone();
        PaymentType paymentType = parsed.getPaymentType() != null ? parsed.getPaymentType() : PaymentType.CASHLESS;
        Instant deliveryTime = parsed.getDeliveryTime();
        String cardToCourierMessage = parsed.getCardToCourierMessage();

        Long orderId = multiCityOrderService.createOrder(
                orderCity,
                orderName,
                itemsToCreate,
                shouldBeFinishedAt,
                kitchenShouldGetOrderAt,
                orderType,
                address,
                customerPhone,
                paymentType,
                deliveryTime,
                cardToCourierMessage
        );

        log.info("Imported order {} in city {} with {} items", orderName, orderCity, itemsToCreate.size());

        return ImportOrderResponse.builder()
                .orderId(orderId)
                .orderName(orderName)
                .city(orderCity.name())
                .build();
    }

    private static MultiCityOrderService.City toOrderCity(MultiCityViewService.City viewCity) {
        return viewCity == MultiCityViewService.City.PARNAS
                ? MultiCityOrderService.City.PARNAS
                : MultiCityOrderService.City.UKHTA;
    }

    private MultiCityViewService.City resolveCity(ImportOrderRequest request) {
        if (request.getCity() != null && !request.getCity().isBlank()) {
            String c = request.getCity().trim().toUpperCase();
            if ("PARNAS".equals(c)) return MultiCityViewService.City.PARNAS;
            if ("UKHTA".equals(c)) return MultiCityViewService.City.UKHTA;
            throw new IllegalArgumentException("Недопустимый город: " + request.getCity() + ". Допустимы: PARNAS, UKHTA");
        }
        ParsedOrderDto parsed = orderTextParserService.parseOrderText(
                request.getText(),
                new ArrayList<>(),
                new ArrayList<>());
        String cityName = parsed.getCity();
        if (cityName != null) {
            if (cityName.toLowerCase().contains("парнас") || cityName.toLowerCase().contains("парголово")) {
                return MultiCityViewService.City.PARNAS;
            }
            if (cityName.toLowerCase().contains("ухта")) {
                return MultiCityViewService.City.UKHTA;
            }
        }
        throw new IllegalArgumentException("Город не указан в запросе и не найден в тексте. Укажите city: PARNAS или UKHTA.");
    }

    private String resolveOrderNumber(ImportOrderRequest request, ParsedOrderDto parsed) {
        if (request.getOrderNumber() != null && !request.getOrderNumber().isBlank()) {
            return request.getOrderNumber().trim();
        }
        return parsed.getOrderNumber() != null ? parsed.getOrderNumber().trim() : null;
    }
}
