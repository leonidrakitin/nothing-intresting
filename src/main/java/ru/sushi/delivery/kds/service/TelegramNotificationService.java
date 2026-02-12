package ru.sushi.delivery.kds.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import ru.sushi.delivery.kds.dto.OrderAddressDto;
import ru.sushi.delivery.kds.dto.OrderShortDto;
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
import jakarta.annotation.PostConstruct;

/**
 * Сервис отправки уведомлений о новых заказах в Telegram-чаты для курьеров.
 * Сообщения отправляются во все чаты из списка chat-ids (и в chat-id, если задан).
 * Telegram не даёт боту получить список чатов автоматически — при добавлении бота в новый чат
 * нужно получить его Chat ID и добавить в конфиг.
 */
@Log4j2
@Service
@ConditionalOnProperty(name = "telegram.bot.token", matchIfMissing = false)
@RequiredArgsConstructor
public class TelegramNotificationService {

    private static final String TELEGRAM_SEND_MESSAGE_URL = "https://api.telegram.org/bot%s/sendMessage";
    private static final String TELEGRAM_SEND_PHOTO_URL = "https://api.telegram.org/bot%s/sendPhoto";
    private static final int CAPTION_MAX_LENGTH = 1024;

    private final StaticMapImageService staticMapImageService;

    @PostConstruct
    public void logTelegramConfig() {
        List<String> chatIds = getChatIds();
        if (botToken == null || botToken.isBlank()) {
            log.warn("Telegram: бот не будет отправлять уведомления — токен не задан (TELEGRAM_BOT_TOKEN пустой или отсутствует).");
        } else if (chatIds.isEmpty()) {
            log.warn("Telegram: бот не будет отправлять уведомления — нет ни одного chat-id (задайте TELEGRAM_CHAT_ID или TELEGRAM_CHAT_IDS).");
        } else {
            log.info("Telegram: уведомления включены, чатов для отправки: {}", chatIds.size());
        }
    }
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
            log.info("Telegram: уведомление не отправлено — не настроен токен или список чатов (chat-id/chat-ids).");
            return;
        }

        String message = buildOrderMessage(
                city, orderName, menuItems, shouldBeFinishedAt,
                kitchenShouldGetOrderAt, orderType, address,
                customerPhone, paymentType, deliveryTime, cardToCourierMessage
        );

        Optional<byte[]> mapImage = tryBuildMapImage(address, orderName);
        for (String chatId : chatIds) {
            if (mapImage.isPresent()) {
                sendPhoto(chatId, mapImage.get(), message);
            } else {
                sendMessage(chatId, message);
            }
        }
    }

    /**
     * Отправляет в Telegram уведомление по уже созданному заказу на доставку (кнопка «Отправить ТГ» во вкладке Доставки).
     *
     * @param order     заказ на доставку
     * @param cityLabel подпись города для сообщения (например «Парнас», «Ухта»); если null — используется город из адреса
     */
    public void notifyExistingOrder(OrderShortDto order, String cityLabel) {
        if (order.getOrderType() != OrderType.DELIVERY) {
            log.debug("Заказ не на доставку — не отправляем в Telegram.");
            return;
        }
        List<String> chatIds = getChatIds();
        if (botToken == null || botToken.isBlank() || chatIds.isEmpty()) {
            log.info("Telegram: уведомление не отправлено — не настроен токен или список чатов.");
            return;
        }
        String message = buildOrderMessageFromDto(order, cityLabel);
        Optional<byte[]> mapImage = tryBuildMapImage(order.getAddress(), order.getName());
        for (String chatId : chatIds) {
            if (mapImage.isPresent()) {
                sendPhoto(chatId, mapImage.get(), message);
            } else {
                sendMessage(chatId, message);
            }
        }
    }

    /** Строит картинку карты с номером заказа, если в адресе есть сохранённые координаты. */
    private Optional<byte[]> tryBuildMapImage(OrderAddressDto address, String orderName) {
        if (address == null || address.getLatitude() == null || address.getLongitude() == null || orderName == null) {
            return Optional.empty();
        }
        return staticMapImageService.buildMapImageWithOrderNumber(
                address.getLongitude(), address.getLatitude(), orderName);
    }

    private String buildOrderMessageFromDto(OrderShortDto order, String cityLabel) {
        OrderAddressDto address = order.getAddress();
        String cityName = cityLabel != null && !cityLabel.isBlank() ? cityLabel : (address != null && address.getCity() != null ? address.getCity() : "Доставка");

        StringBuilder sb = new StringBuilder();
        sb.append("📋 Номер: ").append(order.getName()).append("\n\n");

        if (address != null) {
            StringBuilder addrFull = new StringBuilder();
            if (address.getStreet() != null) addrFull.append(address.getStreet());
            if (address.getHouse() != null) addrFull.append(", д. ").append(address.getHouse());
            if (address.getFlat() != null) addrFull.append(", кв. ").append(address.getFlat());
            if (address.getFloor() != null) addrFull.append(", эт. ").append(address.getFloor());
            if (address.getEntrance() != null) addrFull.append(", под. ").append(address.getEntrance());
            String fullAddr = addrFull.toString().replaceFirst("^, ", "").trim();
            String yandexUrl = buildYandexMapsRouteUrl(address, (cityName + " " + fullAddr).trim());
            sb.append("[📍 Проложить маршрут](").append(yandexUrl).append(")\n");

            sb.append("Адрес: ");
            if (address.getStreet() != null) sb.append(address.getStreet());
            if (address.getHouse() != null) sb.append(address.getStreet() != null ? ", д. " : "д. ").append(address.getHouse());
            sb.append("\n");
            if (address.getFlat() != null) sb.append("       кв. ").append(address.getFlat()).append("\n");
            if (address.getFloor() != null) sb.append("       эт. ").append(address.getFloor()).append("\n");
            if (address.getEntrance() != null) sb.append("       под. ").append(address.getEntrance()).append("\n");
        }

        if (address != null && address.getComment() != null && !address.getComment().isBlank()) {
            sb.append("📝 Комментарий к заказу: ").append(address.getComment()).append("\n");
        }

        if (order.getCustomerPhone() != null && !order.getCustomerPhone().isBlank()) {
            sb.append("📞 Телефон: ").append(order.getCustomerPhone()).append("\n");
        }

        if (order.getPaymentType() != null) {
            String paymentStr = order.getPaymentType() == PaymentType.CASH
                    ? "Наличные"
                    : order.getPaymentType() == PaymentType.CASHLESS ? "Оплачено" : "Оплата картой";
            sb.append("💳 Оплата: ").append(paymentStr).append("\n");
        }

        if (order.getDeliveryTime() != null) {
            sb.append("🚚 Время доставки: *").append(TIME_FORMAT.format(order.getDeliveryTime())).append("*\n");
        }

        return sb.toString();
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

        StringBuilder sb = new StringBuilder();
        sb.append("📋 Номер: ").append(orderName).append("\n").append("\n");

        if (orderType == OrderType.DELIVERY && address != null) {
            StringBuilder addrFull = new StringBuilder();
            if (address.getStreet() != null) addrFull.append(address.getStreet());
            if (address.getHouse() != null) addrFull.append(", д. ").append(address.getHouse());
            if (address.getFlat() != null) addrFull.append(", кв. ").append(address.getFlat());
            if (address.getFloor() != null) addrFull.append(", эт. ").append(address.getFloor());
            if (address.getEntrance() != null) addrFull.append(", под. ").append(address.getEntrance());
            String fullAddr = addrFull.toString().replaceFirst("^, ", "").trim();
            String cityPart = cityName.equals("Парнас") ? "Санкт-Петербург" : cityName;
            String yandexUrl = buildYandexMapsRouteUrl(address, (cityPart + " " + fullAddr).trim());
            sb.append("[📍 Проложить маршрут](").append(yandexUrl).append(")\n");

            sb.append("Адрес: ");
            if (address.getStreet() != null) sb.append(address.getStreet());
            if (address.getHouse() != null) sb.append(address.getStreet() != null ? ", д. " : "д. ").append(address.getHouse());
            sb.append("\n");
            if (address.getFlat() != null) sb.append("       кв. ").append(address.getFlat()).append("\n");
            if (address.getFloor() != null) sb.append("       эт. ").append(address.getFloor()).append("\n");
            if (address.getEntrance() != null) sb.append("       под. ").append(address.getEntrance()).append("\n");
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

        if (deliveryTime != null) {
            sb.append("🚚 Время доставки: *").append(TIME_FORMAT.format(deliveryTime)).append("*\n");
        }

        if (cardToCourierMessage != null && !cardToCourierMessage.isBlank()) {
            sb.append("\n").append(cardToCourierMessage);
        }

        return sb.toString();
    }

    /**
     * Ссылка «Проложить маршрут» в Яндекс.Карты.
     * Если в адресе есть координаты (сохранённые при создании заказа) — используем rtext=~lat,lon (маршрут до точки).
     * Иначе — поиск по тексту адреса (?text=...).
     */
    private static String buildYandexMapsRouteUrl(OrderAddressDto address, String fallbackAddressText) {
        if (address != null && address.getLatitude() != null && address.getLongitude() != null) {
            String rtext = "~" + address.getLatitude() + "," + address.getLongitude();
            return "https://yandex.ru/maps/?rtext=" + URLEncoder.encode(rtext, StandardCharsets.UTF_8);
        }
        return "https://yandex.ru/maps/?text=" + URLEncoder.encode(fallbackAddressText != null ? fallbackAddressText : "", StandardCharsets.UTF_8);
    }

    private void sendPhoto(String chatId, byte[] photoBytes, String caption) {
        try {
            String url = String.format(TELEGRAM_SEND_PHOTO_URL, botToken);
            if (caption != null && caption.length() > CAPTION_MAX_LENGTH) {
                caption = caption.substring(0, CAPTION_MAX_LENGTH - 3) + "...";
            }
            String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
            java.io.ByteArrayOutputStream body = new java.io.ByteArrayOutputStream();
            java.io.OutputStreamWriter w = new java.io.OutputStreamWriter(body, StandardCharsets.UTF_8);
            String crlf = "\r\n";
            appendPart(body, w, boundary, "chat_id", chatId, crlf);
            appendPart(body, w, boundary, "caption", caption != null ? caption : "", crlf);
            appendPart(body, w, boundary, "parse_mode", "Markdown", crlf);
            w.write("--" + boundary + crlf);
            w.write("Content-Disposition: form-data; name=\"photo\"; filename=\"map.png\"" + crlf);
            w.write("Content-Type: image/png" + crlf);
            w.write(crlf);
            w.flush();
            body.write(photoBytes);
            body.write((crlf + "--" + boundary + "--" + crlf).getBytes(StandardCharsets.UTF_8));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .timeout(Duration.ofSeconds(15))
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body.toByteArray()))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                log.error("Telegram sendPhoto error for chat {}: {} - {}", chatId, response.statusCode(), response.body());
            } else {
                log.info("Telegram: фото с картой отправлено в чат {}", chatId);
            }
        } catch (Exception e) {
            log.error("Failed to send Telegram photo to chat {}", chatId, e);
        }
    }

    private static void appendPart(java.io.ByteArrayOutputStream body, java.io.OutputStreamWriter w,
                                   String boundary, String name, String value, String crlf) throws java.io.IOException {
        w.write("--" + boundary + crlf);
        w.write("Content-Disposition: form-data; name=\"" + name + "\"" + crlf);
        w.write(crlf);
        w.write(value);
        w.write(crlf);
        w.flush();
    }

    private void sendMessage(String chatId, String text) {
        try {
            String url = String.format(TELEGRAM_SEND_MESSAGE_URL, botToken);
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
                log.info("Telegram: уведомление отправлено в чат {}", chatId);
            }
        } catch (Exception e) {
            log.error("Failed to send Telegram notification to chat {}", chatId, e);
        }
    }
}
