package ru.sushi.delivery.kds.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sushi.delivery.kds.domain.persist.entity.ItemCombo;
import ru.sushi.delivery.kds.domain.persist.entity.product.MenuItem;
import ru.sushi.delivery.kds.model.PaymentType;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class OrderTextParserServiceTest {

    private final OrderTextParserService parser = new OrderTextParserService();

    /**
     * Текст заказа из Starter в формате "💵Наличными курьеру: 2310 P".
     * Раньше парсер не распознавал этот вид оплаты и подставлял CASHLESS по умолчанию.
     */
    private static final String ORDER_8627_STARTER_CASH =
            """
            🚙Оформлен заказ 8627 · Starter ID 8627
            Доставка · Кухня Ухта, Оплеснина 10
            Зона: Город

            🕒К 17:14 – 17:34, 01.03.2026
            улица Дзержинского, 41, 1 подъезд, 16 домофон, 2 этаж, кв. 16

            · 1× Сет На двоих – 1850 P
            · 1× Креветки – 460 P

            · 3× Соевый соус  – Бесплатно
            · 3× Имбирь – Бесплатно
            · 3× Палочки – Бесплатно
            · 3× Васаби – Бесплатно

            +79129657419
            Алёна · 2й заказ · iOS

            💵Наличными курьеру: 2310 P
            Сумма заказа: 2310 P
            """;

    @Test
    void parsePaymentType_cashWhenNalichnymiKurieru() {
        List<MenuItem> menuItems = Collections.emptyList();
        List<ItemCombo> combos = Collections.emptyList();

        var parsed = parser.parseOrderText(ORDER_8627_STARTER_CASH, menuItems, combos);

        assertEquals(PaymentType.CASH, parsed.getPaymentType(),
                "Формат «💵Наличными курьеру: 2310 P» из Starter должен парситься как наличные");
    }
}
