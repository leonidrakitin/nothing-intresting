package ru.sushi.delivery.kds.service;

import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import ru.sushi.delivery.kds.dto.OrderAddressDto;
import ru.sushi.delivery.kds.domain.persist.entity.product.MenuItem;
import ru.sushi.delivery.kds.model.OrderType;
import ru.sushi.delivery.kds.model.PaymentType;
import ru.sushi.delivery.kds.service.MultiCityOrderService.City;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Сервис отправки уведомлений о новых заказах в Telegram-чаты для курьеров.
 * Сообщения отправляются во все чаты из списка chat-ids (и в chat-id, если задан).
 * Telegram не даёт боту получить список чатов автоматически — при добавлении бота в новый чат
 * нужно получить его Chat ID и добавить в конфиг.
 */
@Log4j2
@Service
@ConditionalOnProperty(name = "telegram.bot.token", matchIfMissing = false)
public class TelegramNotificationService {

    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot%s/sendMessage";
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy")
            .withZone(ZoneId.systemDefault());

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.chat-id:}")
    private String singleChatId;

    /** Список chat ID через запятую или в YAML как массив. Пример: -1001,-1002 или TELEGRAM_CHAT_IDS=-1001,-1002 */
    @Value("${telegram.bot.chat-ids:}")
    private String chatIdsConfig;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /** Собирает все уникальные chat ID: из chat-id и из chat-ids (строка через запятую). */
    private List<String> getChatIds() {
        LinkedHashSet<String> ids = new LinkedHashSet<>();
        if (singleChatId != null && !singleChatId.isBlank()) {
            ids.add(singleChatId.trim());
        }
        if (chatIdsConfig != null && !chatIdsConfig.isBlank()) {
            Arrays.stream(chatIdsConfig.split("[,;\\s]+"))
                    .map(String::trim)
                    .filter(id -> !id.isEmpty())
                    .forEach(ids::add);
        }
        return new ArrayList<>(ids);
    }

    /**
     * Отправляет уведомление о новом заказе в Telegram-чат курьеров.
     */
    public void notifyNewOrder(
            City city,
            String orderName,
            List<MenuItem> menuItems,
            Instant shouldBeFinishedAt,
            Instant kitchenShouldGetOrderAt,
            OrderType orderType,
            OrderAddressDto address,
            String customerPhone,
            PaymentType paymentType,
            Instant deliveryTime,
            String cardToCourierMessage
    ) {
        if (orderType == null || orderType == OrderType.PICKUP) {
            log.debug("Самовывоз — не отправляем уведомление в Telegram.");
            return;
        }

        List<String> chatIds = getChatIds();
        if (botToken == null || botToken.isBlank() || chatIds.isEmpty()) {
            log.debug("Telegram not configured (token or chat-ids missing). Skipping notification.");
            return;
        }

        String message = buildOrderMessage(
                city, orderName, menuItems, shouldBeFinishedAt,
                kitchenShouldGetOrderAt, orderType, address,
                customerPhone, paymentType, deliveryTime, cardToCourierMessage
        );

        for (String chatId : chatIds) {
            sendMessage(chatId, message);
        }
    }

    private String buildOrderMessage(
            City city,
            String orderName,
            List<MenuItem> menuItems,
            Instant shouldBeFinishedAt,
            Instant kitchenShouldGetOrderAt,
            OrderType orderType,
            OrderAddressDto address,
            String customerPhone,
            PaymentType paymentType,
            Instant deliveryTime,
            String cardToCourierMessage
    ) {
        String cityName = city == City.PARNAS ? "Парнас" : "Ухта";
        String orderTypeStr = orderType == OrderType.DELIVERY ? "Доставка" : "Самовывоз";

        StringBuilder sb = new StringBuilder();
        sb.append("🆕 Новый заказ!\n\n");
        sb.append("📋 Номер: ").append(orderName).append("\n");
        sb.append("🏙 Город: ").append(cityName).append("\n");
        sb.append("📦 Тип: ").append(orderTypeStr).append("\n");

        if (orderType == OrderType.DELIVERY && address != null) {
            sb.append("📍 Адрес: ");
            if (address.getStreet() != null) sb.append(address.getStreet());
            if (address.getHouse() != null) sb.append(", д. ").append(address.getHouse());
            if (address.getFlat() != null) sb.append(", кв. ").append(address.getFlat());
            if (address.getFloor() != null) sb.append(", эт. ").append(address.getFloor());
            if (address.getEntrance() != null) sb.append(", подъезд ").append(address.getEntrance());
            sb.append("\n");
        }

        if (address != null && address.getComment() != null && !address.getComment().isBlank()) {
            sb.append("📝 Комментарий к заказу: ").append(address.getComment()).append("\n");
        }

        if (customerPhone != null && !customerPhone.isBlank()) {
            sb.append("📞 Телефон: ").append(customerPhone).append("\n");
        }

        if (paymentType != null) {
            String paymentStr = paymentType == PaymentType.CASH
                    ? "Наличные"
                    : paymentType == PaymentType.CASHLESS ? "Оплачено" : "Оплата картой";

            sb.append("💳 Оплата: ").append(paymentStr).append("\n");
        }

        if (kitchenShouldGetOrderAt != null) {
            sb.append("⏰ Время начала: ").append(TIME_FORMAT.format(kitchenShouldGetOrderAt)).append("\n");
        }
        if (shouldBeFinishedAt != null) {
            sb.append("✅ Готовность к: ").append(TIME_FORMAT.format(shouldBeFinishedAt)).append("\n");
        }
        if (deliveryTime != null) {
            sb.append("🚚 Время доставки: *").append(TIME_FORMAT.format(deliveryTime)).append("*\n");
        }

        if (cardToCourierMessage != null && !cardToCourierMessage.isBlank()) {
            sb.append("\n").append(cardToCourierMessage);
        }

        return sb.toString();
    }

    private void sendMessage(String chatId, String text) {
        try {
            String url = String.format(TELEGRAM_API_URL, botToken);
            String body = "chat_id=" + URLEncoder.encode(chatId, StandardCharsets.UTF_8)
                    + "&parse_mode=Markdown"
                    + "&text=" + URLEncoder.encode(text, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Telegram API error for chat {}: {} - {}", chatId, response.statusCode(), response.body());
            } else {
                log.debug("Telegram notification sent to chat {}", chatId);
            }
        } catch (Exception e) {
            log.error("Failed to send Telegram notification to chat {}", chatId, e);
        }
    }
}
