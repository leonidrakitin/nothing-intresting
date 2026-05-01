package ru.sushi.delivery.kds.service;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.persist.entity.product.MenuItem;
import ru.sushi.delivery.kds.dto.OrderAddressDto;
import ru.sushi.delivery.kds.dto.OrderShortDto;
import ru.sushi.delivery.kds.model.OrderType;
import ru.sushi.delivery.kds.model.PaymentType;
import ru.sushi.delivery.kds.service.MultiCityOrderService.City;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

@Log4j2
@Service
@ConditionalOnProperty(name = "vk.bot.token", matchIfMissing = false)
public class VkNotificationService {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy")
            .withZone(ZoneId.systemDefault());

    @Value("${vk.bot.token}")
    private String botToken;

    @Value("${vk.bot.group-id:0}")
    private Integer groupId;

    @Value("${vk.bot.peer-id:}")
    private String singlePeerId;

    @Value("${vk.bot.peer-ids:}")
    private String peerIdsConfig;

    private final VkApiClient vkApiClient = new VkApiClient(HttpTransportClient.getInstance());

    @PostConstruct
    public void logVkConfig() {
        List<Long> peerIds = getPeerIds();
        if (botToken == null || botToken.isBlank()) {
            log.warn("VK: бот не будет отправлять уведомления — токен не задан.");
            return;
        }
        if (groupId == null || groupId <= 0) {
            log.warn("VK: бот не будет отправлять уведомления — не задан vk.bot.group-id.");
            return;
        }
        if (peerIds.isEmpty()) {
            log.warn("VK: бот не будет отправлять уведомления — не задан vk.bot.peer-id/vk.bot.peer-ids.");
            return;
        }
        log.info("VK: уведомления включены, получателей: {}", peerIds.size());
    }

    public boolean notifyNewOrder(
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
            log.debug("Самовывоз — не отправляем уведомление в VK.");
            return false;
        }
        GroupActor actor = buildActor();
        List<Long> peerIds = getPeerIds();
        if (actor == null || peerIds.isEmpty()) {
            log.info("VK: уведомление не отправлено — не настроен токен/group-id или список peer-id.");
            return false;
        }

        String message = buildOrderMessage(
                actor,
                city, orderName, menuItems, shouldBeFinishedAt, kitchenShouldGetOrderAt,
                orderType, address, customerPhone, paymentType, deliveryTime, cardToCourierMessage
        );
        boolean sent = false;
        for (Long peerId : peerIds) {
            sent = sendMessage(actor, peerId, message) || sent;
        }
        return sent;
    }

    public boolean notifyExistingOrder(OrderShortDto order, String cityLabel) {
        if (order.getOrderType() != OrderType.DELIVERY) {
            log.debug("Заказ не на доставку — не отправляем в VK.");
            return false;
        }
        GroupActor actor = buildActor();
        List<Long> peerIds = getPeerIds();
        if (actor == null || peerIds.isEmpty()) {
            log.info("VK: уведомление не отправлено — не настроен токен/group-id или список peer-id.");
            return false;
        }

        String message = buildOrderMessageFromDto(order, cityLabel, actor);
        boolean sent = false;
        for (Long peerId : peerIds) {
            sent = sendMessage(actor, peerId, message) || sent;
        }
        return sent;
    }

    private GroupActor buildActor() {
        if (botToken == null || botToken.isBlank() || groupId == null || groupId <= 0) {
            return null;
        }
        return new GroupActor(groupId.longValue(), botToken);
    }

    private List<Long> getPeerIds() {
        LinkedHashSet<Long> ids = new LinkedHashSet<>();
        addPeerId(ids, singlePeerId);
        if (peerIdsConfig != null && !peerIdsConfig.isBlank()) {
            Arrays.stream(peerIdsConfig.split("[,;\\s]+"))
                    .map(String::trim)
                    .forEach(peerId -> addPeerId(ids, peerId));
        }
        return new ArrayList<>(ids);
    }

    private void addPeerId(LinkedHashSet<Long> ids, String rawId) {
        if (rawId == null || rawId.isBlank()) {
            return;
        }
        try {
            ids.add(Long.parseLong(rawId.trim()));
        } catch (NumberFormatException ex) {
            log.warn("VK: некорректный peer-id '{}', пропускаем.", rawId);
        }
    }

    private String buildOrderMessageFromDto(OrderShortDto order, String cityLabel, GroupActor actor) {
        OrderAddressDto address = order.getAddress();
        String cityName = cityLabel != null && !cityLabel.isBlank()
                ? cityLabel
                : (address != null && address.getCity() != null ? address.getCity() : "Доставка");

        StringBuilder sb = new StringBuilder();
        sb.append("Номер: ").append(order.getName()).append("\n\n");

        if (address != null) {
            StringBuilder addrFull = new StringBuilder();
            if (address.getStreet() != null) addrFull.append(address.getStreet());
            if (address.getHouse() != null) addrFull.append(", д. ").append(address.getHouse());
            if (address.getFlat() != null) addrFull.append(", кв. ").append(address.getFlat());
            if (address.getFloor() != null) addrFull.append(", эт. ").append(address.getFloor());
            if (address.getEntrance() != null) addrFull.append(", под. ").append(address.getEntrance());
            String fullAddr = addrFull.toString().replaceFirst("^, ", "").trim();
            String yandexUrl = buildYandexMapsRouteUrl(address, (cityName + " " + fullAddr).trim());
            yandexUrl = shortenUrlIfPossible(actor, yandexUrl);
            sb.append("Проложить маршрут: ").append(yandexUrl).append("\n");

            sb.append("Адрес: ");
            if (address.getStreet() != null) sb.append(address.getStreet());
            if (address.getHouse() != null) sb.append(address.getStreet() != null ? ", д. " : "д. ").append(address.getHouse());
            sb.append("\n");
            if (address.getFlat() != null) sb.append("       кв. ").append(address.getFlat()).append("\n");
            if (address.getFloor() != null) sb.append("       эт. ").append(address.getFloor()).append("\n");
            if (address.getEntrance() != null) sb.append("       под. ").append(address.getEntrance()).append("\n");
        }

        if (address != null && address.getComment() != null && !address.getComment().isBlank()) {
            sb.append("Комментарий к заказу: ").append(address.getComment()).append("\n");
        }
        if (order.getCustomerPhone() != null && !order.getCustomerPhone().isBlank()) {
            sb.append("Телефон: ").append(order.getCustomerPhone()).append("\n");
        }
        if (order.getPaymentType() != null) {
            String paymentStr = order.getPaymentType() == PaymentType.CASH
                    ? "Наличными курьеру"
                    : order.getPaymentType() == PaymentType.CASHLESS ? "Оплачено" : "Оплата картой";
            sb.append("Оплата: ").append(paymentStr).append("\n");
        }
        if (order.getDeliveryTime() != null) {
            sb.append("Время доставки: ").append(TIME_FORMAT.format(order.getDeliveryTime())).append("\n");
        }
        return sb.toString();
    }

    private String buildOrderMessage(
            GroupActor actor,
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
        sb.append("Номер: ").append(orderName).append("\n\n");

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
            yandexUrl = shortenUrlIfPossible(actor, yandexUrl);
            sb.append("Проложить маршрут: ").append(yandexUrl).append("\n");

            sb.append("Адрес: ");
            if (address.getStreet() != null) sb.append(address.getStreet());
            if (address.getHouse() != null) sb.append(address.getStreet() != null ? ", д. " : "д. ").append(address.getHouse());
            sb.append("\n");
            if (address.getFlat() != null) sb.append("       кв. ").append(address.getFlat()).append("\n");
            if (address.getFloor() != null) sb.append("       эт. ").append(address.getFloor()).append("\n");
            if (address.getEntrance() != null) sb.append("       под. ").append(address.getEntrance()).append("\n");
        }

        if (address != null && address.getComment() != null && !address.getComment().isBlank()) {
            sb.append("Комментарий к заказу: ").append(address.getComment()).append("\n");
        }
        if (customerPhone != null && !customerPhone.isBlank()) {
            sb.append("Телефон: ").append(customerPhone).append("\n");
        }
        if (paymentType != null) {
            String paymentStr = paymentType == PaymentType.CASH
                    ? "Наличными курьеру"
                    : paymentType == PaymentType.CASHLESS ? "Оплачено" : "Оплата картой";
            sb.append("Оплата: ").append(paymentStr).append("\n");
        }
        if (deliveryTime != null) {
            sb.append("Время доставки: ").append(TIME_FORMAT.format(deliveryTime)).append("\n");
        }
        if (cardToCourierMessage != null && !cardToCourierMessage.isBlank()) {
            sb.append("\n").append(cardToCourierMessage);
        }
        return sb.toString();
    }

    private static String buildYandexMapsRouteUrl(OrderAddressDto address, String fallbackAddressText) {
        String encodedText = URLEncoder.encode(fallbackAddressText != null ? fallbackAddressText : "", StandardCharsets.UTF_8);
        if (address != null && address.getLatitude() != null && address.getLongitude() != null) {
            String rtext = "~" + address.getLatitude() + "," + address.getLongitude();
            return "https://yandex.ru/maps/?rtext=" + URLEncoder.encode(rtext, StandardCharsets.UTF_8)
                    + "&text=" + encodedText;
        }
        return "https://yandex.ru/maps/?text=" + encodedText;
    }

    private String shortenUrlIfPossible(GroupActor actor, String url) {
        if (url == null || url.isBlank() || actor == null) {
            return url;
        }
        try {
            var shortUrl = vkApiClient.utils().getShortLink(actor, url).execute().getShortUrl();
            String shortUrlString = shortUrl != null ? shortUrl.toString() : null;
            return (shortUrlString == null || shortUrlString.isBlank()) ? url : shortUrlString;
        } catch (ApiException | ClientException e) {
            log.warn("VK: не удалось сократить ссылку, используем длинную: {}", e.getMessage());
            return url;
        }
    }

    private boolean sendMessage(GroupActor actor, Long peerId, String text) {
        try {
            vkApiClient.messages()
                    .sendDeprecated(actor)
                    .peerId(peerId)
                    .randomId((int) (System.currentTimeMillis() & Integer.MAX_VALUE))
                    .message(text)
                    .execute();
            log.info("VK: уведомление отправлено в peer {}", peerId);
            return true;
        } catch (ApiException | ClientException e) {
            log.error("VK: ошибка отправки в peer {}", peerId, e);
            return false;
        }
    }
}
