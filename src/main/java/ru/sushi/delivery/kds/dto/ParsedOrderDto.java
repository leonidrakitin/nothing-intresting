package ru.sushi.delivery.kds.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sushi.delivery.kds.domain.persist.entity.ItemCombo;
import ru.sushi.delivery.kds.domain.persist.entity.product.MenuItem;
import ru.sushi.delivery.kds.model.OrderType;
import ru.sushi.delivery.kds.model.PaymentType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParsedOrderDto {
    private String orderNumber;
    private String comment;
    @Builder.Default
    private List<ParsedItem> items = new ArrayList<>();
    @Builder.Default
    private List<ParsedCombo> combos = new ArrayList<>();
    @Builder.Default
    private Map<String, Integer> extras = new java.util.HashMap<>(); // Название -> количество
    private Instant kitchenStartTime; // Может быть null
    private Instant finishTime; // Может быть null
    private Integer instrumentsCount; // Количество приборов
    private OrderType orderType; // Тип заказа: PICKUP/DELIVERY
    private String customerPhone; // Телефон клиента
    private PaymentType paymentType; // Тип оплаты
    private OrderAddressDto address; // Адрес доставки
    private Instant deliveryTime; // Время доставки (только для доставки)
    private String city; // Город: "Ухта" или "Парнас"
    /** Сообщение вида "💳Картой курьеру: 3176 P🔸Возьмите терминал" при оплате картой курьеру */
    private String cardToCourierMessage;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParsedItem {
        private String name;
        private Integer quantity;
        private MenuItem menuItem; // Найденный MenuItem, может быть null
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParsedCombo {
        private String name;
        private Integer quantity;
        private ItemCombo combo; // Найденный ItemCombo, может быть null
    }
}

