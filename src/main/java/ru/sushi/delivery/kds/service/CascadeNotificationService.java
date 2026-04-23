package ru.sushi.delivery.kds.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.persist.entity.product.MenuItem;
import ru.sushi.delivery.kds.dto.OrderAddressDto;
import ru.sushi.delivery.kds.dto.OrderShortDto;
import ru.sushi.delivery.kds.model.OrderType;
import ru.sushi.delivery.kds.model.PaymentType;
import ru.sushi.delivery.kds.service.MultiCityOrderService.City;

import java.time.Instant;
import java.util.List;

@Log4j2
@Service
public class CascadeNotificationService {

    @Autowired(required = false)
    private TelegramNotificationService telegramNotificationService;

    @Autowired(required = false)
    private VkNotificationService vkNotificationService;

    public CascadeNotificationResult notifyNewOrder(
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
        boolean telegramSent = sendTelegramNewOrder(
                city, orderName, menuItems, shouldBeFinishedAt, kitchenShouldGetOrderAt,
                orderType, address, customerPhone, paymentType, deliveryTime, cardToCourierMessage
        );
        boolean vkSent = sendVkNewOrder(
                city, orderName, menuItems, shouldBeFinishedAt, kitchenShouldGetOrderAt,
                orderType, address, customerPhone, paymentType, deliveryTime, cardToCourierMessage
        );
        return new CascadeNotificationResult(telegramSent, vkSent);
    }

    public CascadeNotificationResult notifyExistingOrder(OrderShortDto order, String cityLabel) {
        boolean telegramSent = sendTelegramExistingOrder(order, cityLabel);
        boolean vkSent = sendVkExistingOrder(order, cityLabel);
        return new CascadeNotificationResult(telegramSent, vkSent);
    }

    private boolean sendTelegramNewOrder(
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
        if (telegramNotificationService == null) {
            log.info("Telegram уведомления отключены (сервис не создан: не задан telegram.bot.token).");
            return false;
        }
        try {
            return telegramNotificationService.notifyNewOrder(
                    city, orderName, menuItems, shouldBeFinishedAt, kitchenShouldGetOrderAt,
                    orderType, address, customerPhone, paymentType, deliveryTime, cardToCourierMessage
            );
        } catch (Exception e) {
            log.warn("Failed to send Telegram notification for order {}", orderName, e);
            return false;
        }
    }

    private boolean sendVkNewOrder(
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
        if (vkNotificationService == null) {
            log.info("VK уведомления отключены (сервис не создан: не задан vk.bot.token).");
            return false;
        }
        try {
            return vkNotificationService.notifyNewOrder(
                    city, orderName, menuItems, shouldBeFinishedAt, kitchenShouldGetOrderAt,
                    orderType, address, customerPhone, paymentType, deliveryTime, cardToCourierMessage
            );
        } catch (Exception e) {
            log.warn("Failed to send VK notification for order {}", orderName, e);
            return false;
        }
    }

    private boolean sendTelegramExistingOrder(OrderShortDto order, String cityLabel) {
        if (telegramNotificationService == null) {
            log.info("Telegram уведомления отключены (сервис не создан: не задан telegram.bot.token).");
            return false;
        }
        try {
            return telegramNotificationService.notifyExistingOrder(order, cityLabel);
        } catch (Exception e) {
            log.warn("Failed to send Telegram notification for order {}", order.getName(), e);
            return false;
        }
    }

    private boolean sendVkExistingOrder(OrderShortDto order, String cityLabel) {
        if (vkNotificationService == null) {
            log.info("VK уведомления отключены (сервис не создан: не задан vk.bot.token).");
            return false;
        }
        try {
            return vkNotificationService.notifyExistingOrder(order, cityLabel);
        } catch (Exception e) {
            log.warn("Failed to send VK notification for order {}", order.getName(), e);
            return false;
        }
    }
}
