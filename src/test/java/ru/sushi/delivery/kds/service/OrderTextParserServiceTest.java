package ru.sushi.delivery.kds.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sushi.delivery.kds.domain.persist.entity.ItemCombo;
import ru.sushi.delivery.kds.domain.persist.entity.product.MenuItem;
import ru.sushi.delivery.kds.model.OrderType;
import ru.sushi.delivery.kds.model.PaymentType;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class OrderTextParserServiceTest {

    private final OrderTextParserService parser = new OrderTextParserService();

    /**
     * Текст заказа из Starter в формате "💵Наличными курьеру: 2310 P".
     * Раньше парсер не распознавал этот вид оплаты и подставлял CASHLESS по умолчанию.
     */
    /**
     * Starter: строка «Доставка: 100 P» в конце не должна подменять адрес строкой после времени;
     * координаты — из ссылки Навигатора при наличии.
     */
    private static final String ORDER_19905_STARTER_DELIVERY_FEE_LINE =
            """
            🚙Оформлен заказ 19905 · Starter ID 19905
            Доставка · Кухня Я Есть Суши Парнас
            Зона: Ближайшие дома

            🕒К 17:30 – 17:50, 01.05.2026
            улица Валерия Гаврилина, 3к1, посёлок Парголово, 1 подъезд, 267 домофон, 21 этаж, кв. 267 (https://yandex.com/navi/?whatshere%5Bpoint%5D=30.331777,60.074513&whatshere%5Bzoom%5D=18)

            · 1× Сет Я есть суши – 2230 P

            +79119992125
            Александр

            🟢Оплачено онлайн: 2330 P
            Сумма заказа: 2230 P
            Доставка: 100 P
            """;

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

    private static final String ORDER_23678_STARTER_PREORDER =
            """
            🚙Оформлен заказ 23678 (https://crm.starterapp.ru/yaestsushi/admin/order?code=23678) · Starter ID 23678
            Доставка · Кухня Я Есть Суши Парнас
            Зона: Ближайшие дома

            ⏰Предзаказ к 18:50 – 19:10, 01.05.2026
            Толубеевский проезд, 34к3, посёлок Парголово, 1 подъезд, 12345 домофон, 24 этаж, кв. 115 (https://yandex.com/navi/?whatshere%5Bpoint%5D=30.354927,60.085209&whatshere%5Bzoom%5D=18&from=navi) «оставить у двери»

            · 1× Лава с курицей – Подарок*
            · 1× Сет Морская Лагуна – 1310 P
            · 1× Лава с креветкой – 575 P

            · 2× Палочки – Бесплатно
            · 1× Соевый соус  – Бесплатно

            Комментарий: оставить у двери

            +79294598080
            Анастасия · 1й заказ · iOS

            🟢Оплачено онлайн: 1985 P
            Сумма заказа: 1885 P
            Доставка: 100 P
            """;

    private static final String CHIBBIS_DELIVERY_PLAIN_FORMAT =
            """
            Новый заказ № 0HNK8M3QH4MG1

            Заведение: Я есть суши, Ухта, улица Оплеснина, 10

            Состав:
            Боул с креветкой  690 x 1шт. = 690 руб
            Лава с креветкой  650 x 1шт. = 650 руб
            Имбирь  30 x 1шт. = 30 балл

            Стоимость заказа: 1340
            Итого: 1340

            Телефон: +79042076807
            Доставка: улица 30 лет Октября 16
            Квартира: 84
            Подъезд: 6
            Этаж: 3

            Оплата: оплачено онлайн
            Количество персон: 1
            Комментарий: Клиент указал, что перезванивать для проверки заказа не требуется.
            """;

    private static final String CHIBBIS_PICKUP_PLAIN_FORMAT =
            """
            Новый заказ № 0HNK8M4327KBE

            Заведение: Я есть суши, Ухта, улица Оплеснина, 10

            Состав:
            Лава с курицей  520 x 1шт. = 520 балл
            Сет Жар-птица  1370 x 1шт. = 1370 руб

            Стоимость заказа: 1370
            Итого: 1370

            Телефон: +79048638148
            Самовывоз: Ухта, улица Оплеснина, 10

            Оплата: оплачено онлайн
            Количество персон: 3
            """;

    private static final String CHIBBIS_DELIVERY_BRACKETS_FORMAT =
            """
            Новый заказ № 0HNK18G8V4K58

            Заведение: Я есть суши, Ухта, улица Оплеснина, 10

            Состав:
            Филадельфия [730 руб] | 730 x 1 = 730 руб
            Лава с курицей [520 балл] | 520 x 1 = 520 балл
            Лагуна [520 руб] | 520 x 1 = 520 руб

            Стоимость заказа: 1250
            Итого: 1250

            Телефон: +79225821384
            Доставка: проспект Космонавтов 5/2
            Квартира: 176
            Подъезд: 4
            Этаж: 4

            Оплата: оплачено онлайн
            Количество персон: 1
            Комментарий: 4 этаж на право .
            Клиент указал, что перезванивать для проверки заказа не требуется.
            """;

    @Test
    void parseStarterFormat_skipsDeliveryFeeLineUsesAddressAndCoordsFromNavi() {
        var parsed = parser.parseOrderText(ORDER_19905_STARTER_DELIVERY_FEE_LINE, Collections.emptyList(), Collections.emptyList());

        assertNotNull(parsed.getAddress());
        assertEquals("улица Валерия Гаврилина", parsed.getAddress().getStreet());
        assertEquals("3к1", parsed.getAddress().getHouse());
        assertEquals("267", parsed.getAddress().getFlat());
        assertEquals("Парнас", parsed.getAddress().getCity());
        assertEquals(60.074513, parsed.getAddress().getLatitude(), 1e-6);
        assertEquals(30.331777, parsed.getAddress().getLongitude(), 1e-6);
    }

    @Test
    void parsePaymentType_cashWhenNalichnymiKurieru() {
        List<MenuItem> menuItems = Collections.emptyList();
        List<ItemCombo> combos = Collections.emptyList();

        var parsed = parser.parseOrderText(ORDER_8627_STARTER_CASH, menuItems, combos);

        assertEquals(PaymentType.CASH, parsed.getPaymentType(),
                "Формат «💵Наличными курьеру: 2310 P» из Starter должен парситься как наличные");
    }

    @Test
    void parseStarterPreorderWithDrivewayAndInlineQuotedComment() {
        var parsed = parser.parseOrderText(ORDER_23678_STARTER_PREORDER, Collections.emptyList(), Collections.emptyList());

        assertEquals(OrderType.DELIVERY, parsed.getOrderType());
        assertEquals("+79294598080", parsed.getCustomerPhone());
        assertEquals(PaymentType.CASHLESS, parsed.getPaymentType());
        assertNotNull(parsed.getAddress());
        assertEquals("Толубеевский проезд", parsed.getAddress().getStreet());
        assertEquals("34к3", parsed.getAddress().getHouse());
        assertEquals("1", parsed.getAddress().getEntrance());
        assertEquals("115", parsed.getAddress().getFlat());
        assertEquals("24", parsed.getAddress().getFloor());
        assertEquals("12345", parsed.getAddress().getDoorphone());
        assertEquals("Парнас", parsed.getAddress().getCity());
        assertEquals(60.085209, parsed.getAddress().getLatitude(), 1e-6);
        assertEquals(30.354927, parsed.getAddress().getLongitude(), 1e-6);
        assertEquals("оставить у двери", parsed.getComment());
    }

    @Test
    void parseNewChibbisPlainDeliveryFormat_itemsExtrasAndAddress() {
        var parsed = parser.parseOrderText(CHIBBIS_DELIVERY_PLAIN_FORMAT, Collections.emptyList(), Collections.emptyList());

        assertEquals("0HNK8M3QH4MG1", parsed.getOrderNumber());
        assertEquals(OrderType.DELIVERY, parsed.getOrderType());
        assertEquals(PaymentType.CASHLESS, parsed.getPaymentType());
        assertEquals("+79042076807", parsed.getCustomerPhone());

        assertEquals(2, parsed.getItems().size());
        assertHasItem(parsed.getItems(), "Боул с креветкой", 1);
        assertHasItem(parsed.getItems(), "Лава с креветкой", 1);

        assertEquals(1, parsed.getExtras().getOrDefault("Имбирь", 0));

        assertNotNull(parsed.getAddress());
        assertEquals("30 лет Октября", parsed.getAddress().getStreet());
        assertEquals("16", parsed.getAddress().getHouse());
    }

    @Test
    void parseNewChibbisPlainPickupFormat_comboAndExtras() {
        var parsed = parser.parseOrderText(CHIBBIS_PICKUP_PLAIN_FORMAT, Collections.emptyList(), Collections.emptyList());

        assertEquals("0HNK8M4327KBE", parsed.getOrderNumber());
        assertEquals(OrderType.PICKUP, parsed.getOrderType());
        assertEquals(PaymentType.CASHLESS, parsed.getPaymentType());
        assertEquals("+79048638148", parsed.getCustomerPhone());

        assertEquals(1, parsed.getCombos().size());
        assertEquals("Сет Жар-птица", parsed.getCombos().get(0).getName());
        assertEquals(1, parsed.getCombos().get(0).getQuantity());

        assertEquals(1, parsed.getItems().size());
        assertHasItem(parsed.getItems(), "Лава с курицей", 1);
    }

    @Test
    void parseNewChibbisBracketFormat_keepsExistingBehavior() {
        var parsed = parser.parseOrderText(CHIBBIS_DELIVERY_BRACKETS_FORMAT, Collections.emptyList(), Collections.emptyList());

        assertEquals("0HNK18G8V4K58", parsed.getOrderNumber());
        assertEquals(OrderType.DELIVERY, parsed.getOrderType());
        assertEquals(PaymentType.CASHLESS, parsed.getPaymentType());
        assertEquals("+79225821384", parsed.getCustomerPhone());

        assertEquals(3, parsed.getItems().size());
        assertHasItem(parsed.getItems(), "Филадельфия", 1);
        assertHasItem(parsed.getItems(), "Лава с курицей", 1);
        assertHasItem(parsed.getItems(), "Лагуна", 1);

        assertEquals(0, parsed.getExtras().getOrDefault("Имбирь", 0));
        assertNotNull(parsed.getAddress());
        assertNotNull(parsed.getAddress().getStreet());
    }

    private void assertHasItem(List<ru.sushi.delivery.kds.dto.ParsedOrderDto.ParsedItem> items, String name, int qty) {
        var found = items.stream().filter(i -> name.equals(i.getName())).findFirst().orElseThrow();
        assertEquals(qty, found.getQuantity());
    }
}
