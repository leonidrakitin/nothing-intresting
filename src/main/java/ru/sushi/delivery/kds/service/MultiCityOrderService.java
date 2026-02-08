package ru.sushi.delivery.kds.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.persist.entity.flow.Station;
import ru.sushi.delivery.kds.domain.persist.entity.product.MenuItem;
import ru.sushi.delivery.kds.dto.OrderAddressDto;
import ru.sushi.delivery.kds.dto.OrderItemDto;
import ru.sushi.delivery.kds.dto.OrderShortDto;
import ru.sushi.delivery.kds.model.FlowStepType;
import ru.sushi.delivery.kds.model.OrderType;
import ru.sushi.delivery.kds.model.PaymentType;
import ru.sushi.delivery.kds.model.OrderItemStationStatus;
import ru.sushi.delivery.kds.model.OrderStatus;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MultiCityOrderService {

    @Qualifier("parnasJdbcTemplate")
    private final JdbcTemplate parnasJdbcTemplate;

    @Qualifier("ukhtaJdbcTemplate")
    private final JdbcTemplate ukhtaJdbcTemplate;

    public enum City {
        PARNAS,
        UKHTA
    }

    /**
     * Проверяет, существует ли заказ с указанным именем за сегодня
     */
    public boolean orderExistsByNameToday(City city, String name) {
        JdbcTemplate template = getTemplate(city);
        Instant now = Instant.now();
        LocalDateTime localDateTime = LocalDateTime.ofInstant(now, ZoneId.systemDefault());
        Instant startOfDay = localDateTime.toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant startOfTomorrow = localDateTime.toLocalDate().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        String sql = """
            SELECT COUNT(*) 
            FROM orders 
            WHERE name = ? 
            AND created_at >= ? 
            AND created_at < ?
            """;

        Integer count = template.queryForObject(
            sql,
            Integer.class,
            name,
            Timestamp.from(startOfDay),
            Timestamp.from(startOfTomorrow)
        );

        return count != null && count > 0;
    }

    /**
     * Создает заказ в БД выбранного города
     */
    public Long createOrder(
            City city,
            String name,
            List<MenuItem> menuItems,
            Instant shouldBeFinishedAt,
            Instant kitchenShouldGetOrderAt,
            OrderType orderType,
            OrderAddressDto address,
            String customerPhone,
            PaymentType paymentType,
            Instant deliveryTime
    ) {
        JdbcTemplate template = getTemplate(city);

        // Проверяем, является ли заказ предзаказом
        Instant now = Instant.now();
        boolean isPreorder = kitchenShouldGetOrderAt != null &&
                            kitchenShouldGetOrderAt.isAfter(now.plusSeconds(15 * 60));

        OrderType orderTypeVal = orderType != null ? orderType : OrderType.PICKUP;
        String orderTypeStr = orderTypeVal.name();
        String paymentTypeStr = paymentType != null ? paymentType.name() : null;

        // Создаем заказ
        String insertOrderSql = """
            INSERT INTO orders (name, status, should_be_finished_at, kitchen_should_get_order_at, 
                              status_update_at, created_at, preorder, order_type, customer_phone, payment_type,
                              address_street, address_flat, address_floor, address_entrance, address_comment,
                              address_city, address_doorphone, address_house, delivery_time)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS);
            int idx = 1;
            ps.setString(idx++, name);
            ps.setString(idx++, OrderStatus.CREATED.name());
            ps.setTimestamp(idx++, shouldBeFinishedAt != null ? Timestamp.from(shouldBeFinishedAt) : null);
            ps.setTimestamp(idx++, kitchenShouldGetOrderAt != null ? Timestamp.from(kitchenShouldGetOrderAt) : null);
            ps.setTimestamp(idx++, Timestamp.from(now));
            ps.setTimestamp(idx++, Timestamp.from(now));
            ps.setBoolean(idx++, isPreorder);
            ps.setString(idx++, orderTypeStr);
            ps.setString(idx++, customerPhone);
            ps.setString(idx++, paymentTypeStr);
            ps.setString(idx++, address != null ? address.getStreet() : null);
            ps.setString(idx++, address != null ? address.getFlat() : null);
            ps.setString(idx++, address != null ? address.getFloor() : null);
            ps.setString(idx++, address != null ? address.getEntrance() : null);
            ps.setString(idx++, address != null ? address.getComment() : null);
            ps.setString(idx++, address != null ? address.getCity() : null);
            ps.setString(idx++, address != null ? address.getDoorphone() : null);
            ps.setString(idx++, address != null ? address.getHouse() : null);
            ps.setTimestamp(idx++, deliveryTime != null ? Timestamp.from(deliveryTime) : null);
            return ps;
        }, keyHolder);

        Long orderId = keyHolder.getKey() != null ? keyHolder.getKey().longValue() : null;
        if (orderId == null) {
            throw new RuntimeException("Failed to create order, no ID generated");
        }

        // Создаем позиции заказа
        String insertOrderItemSql = """
            INSERT INTO order_item (order_id, menu_item_id, current_flow_step, status, 
                                   status_updated_at, station_changed_at)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        for (MenuItem menuItem : menuItems) {
            template.update(insertOrderItemSql,
                orderId,
                menuItem.getId(),
                1, // current_flow_step по умолчанию
                OrderItemStationStatus.ADDED.name(),
                Timestamp.from(now),
                Timestamp.from(now)
            );
        }

        log.info("Created order {} in city {} with {} items", name, city, menuItems.size());
        return orderId;
    }

    /**
     * Получает все активные заказы с позициями
     */
    public List<OrderShortDto> getAllActiveOrdersWithItems(City city) {
        JdbcTemplate template = getTemplate(city);

        String sql = """
            SELECT o.id, o.name, o.status, o.should_be_finished_at, 
                   o.kitchen_should_get_order_at, o.kitchen_got_order_at, o.preorder,
                   o.order_type, o.customer_phone, o.payment_type,
                   o.address_street, o.address_flat, o.address_floor, o.address_entrance,
                   o.address_comment, o.address_city, o.address_doorphone, o.address_house,
                   o.delivery_time
            FROM orders o
            WHERE o.status IN ('CREATED', 'COOKING', 'COLLECTING')
            ORDER BY o.kitchen_should_get_order_at ASC
            """;

        return template.query(sql, (rs, rowNum) -> {
            Long orderId = rs.getLong("id");
            List<OrderItemDto> orderItems = getOrderItems(city, orderId);
            return buildOrderShortDto(rs, orderItems);
        });
    }

    /**
     * Получает все заказы за указанный период
     */
    public List<OrderShortDto> getAllOrdersWithItemsBetweenDates(City city, Instant from, Instant to) {
        JdbcTemplate template = getTemplate(city);

        String sql = """
            SELECT o.id, o.name, o.status, o.should_be_finished_at, 
                   o.kitchen_should_get_order_at, o.kitchen_got_order_at, o.preorder,
                   o.order_type, o.customer_phone, o.payment_type,
                   o.address_street, o.address_flat, o.address_floor, o.address_entrance,
                   o.address_comment, o.address_city, o.address_doorphone, o.address_house,
                   o.delivery_time
            FROM orders o
            WHERE o.created_at >= ? AND o.created_at < ?
            ORDER BY o.created_at DESC
            """;

        return template.query(sql, (rs, rowNum) -> {
            Long orderId = rs.getLong("id");
            List<OrderItemDto> orderItems = getOrderItems(city, orderId);
            return buildOrderShortDto(rs, orderItems);
        }, Timestamp.from(from), Timestamp.from(to));
    }

    /**
     * Получает позиции заказа
     */
    public List<OrderItemDto> getOrderItems(City city, Long orderId) {
        JdbcTemplate template = getTemplate(city);

        String sql = """
            SELECT oi.id, oi.order_id, oi.menu_item_id, oi.current_flow_step, 
                   oi.status, oi.status_updated_at, oi.station_changed_at,
                   mi.name as menu_item_name, mi.flow_id, pt.extra,
                   fs.station_id, s.name as station_name, fs.step_type,
                   o.name as order_name
            FROM order_item oi
            JOIN menu_item mi ON oi.menu_item_id = mi.id
            JOIN product_type pt ON mi.product_type_id = pt.id
            JOIN orders o ON oi.order_id = o.id
            LEFT JOIN flow_step fs ON fs.flow_id = mi.flow_id AND fs.step_order = oi.current_flow_step
            LEFT JOIN station s ON fs.station_id = s.id
            WHERE oi.order_id = ?
            ORDER BY pt.priority ASC, oi.id ASC
            """;

        return template.query(sql, (rs, rowNum) -> {
            Station currentStation = null;
            if (rs.getObject("station_id") != null) {
                currentStation = Station.builder()
                    .id(rs.getLong("station_id"))
                    .name(rs.getString("station_name"))
                    .build();
            }

            FlowStepType flowStepType = null;
            if (rs.getObject("step_type") != null) {
                flowStepType = FlowStepType.valueOf(rs.getString("step_type"));
            }

            return OrderItemDto.builder()
                .id(rs.getLong("id"))
                .orderId(rs.getLong("order_id"))
                .orderName(rs.getString("order_name"))
                .name(rs.getString("menu_item_name"))
                .ingredients(new ArrayList<>()) // Ингредиенты можно загрузить отдельно, если нужно
                .flowId(rs.getLong("flow_id"))
                .status(OrderItemStationStatus.valueOf(rs.getString("status")))
                .statusUpdatedAt(rs.getTimestamp("status_updated_at") != null ?
                    rs.getTimestamp("status_updated_at").toInstant() : null)
                .createdAt(rs.getTimestamp("status_updated_at") != null ?
                    rs.getTimestamp("status_updated_at").toInstant() : null)
                .timeToCook(180) // Значение по умолчанию
                .extra(rs.getBoolean("extra"))
                .currentStation(currentStation)
                .flowStepType(flowStepType)
                .build();
        }, orderId);
    }

    /**
     * Обновляет время начала приготовления заказа
     */
    public void updateKitchenShouldGetOrderAt(City city, Long orderId, Instant newInstant) {
        JdbcTemplate template = getTemplate(city);

        Instant now = Instant.now();
        boolean isPreorder = newInstant != null && newInstant.isAfter(now.plusSeconds(15 * 60));

        String sql = """
            UPDATE orders 
            SET kitchen_should_get_order_at = ?, preorder = ?
            WHERE id = ?
            """;

        template.update(sql,
            newInstant != null ? Timestamp.from(newInstant) : null,
            isPreorder,
            orderId
        );
    }

    /**
     * Обновляет имя заказа
     */
    public void updateOrderName(City city, Long orderId, String newName) {
        JdbcTemplate template = getTemplate(city);

        String sql = """
            UPDATE orders 
            SET name = ?
            WHERE id = ?
            """;

        template.update(sql, newName, orderId);
    }

    /**
     * Отменяет заказ
     */
    public void cancelOrder(City city, Long orderId) {
        JdbcTemplate template = getTemplate(city);

        // Обновляем статус заказа
        String updateOrderSql = """
            UPDATE orders 
            SET status = ?
            WHERE id = ?
            """;
        template.update(updateOrderSql, OrderStatus.CANCELED.name(), orderId);

        // Отменяем все позиции заказа
        String updateOrderItemsSql = """
            UPDATE order_item 
            SET status = ?, current_flow_step = ?
            WHERE order_id = ?
            """;
        template.update(updateOrderItemsSql,
            OrderItemStationStatus.CANCELED.name(),
            0, // CANCEL_STEP_ORDER
            orderId
        );
    }

    /**
     * Отменяет позицию заказа
     */
    public void cancelOrderItem(City city, Long orderItemId) {
        JdbcTemplate template = getTemplate(city);

        String sql = """
            UPDATE order_item 
            SET status = ?, current_flow_step = ?
            WHERE id = ?
            """;

        template.update(sql,
            OrderItemStationStatus.CANCELED.name(),
            0, // CANCEL_STEP_ORDER
            orderItemId
        );
    }

    /**
     * Добавляет позицию в заказ
     */
    public void addItemToOrder(City city, Long orderId, MenuItem menuItem) {
        JdbcTemplate template = getTemplate(city);

        String sql = """
            INSERT INTO order_item (order_id, menu_item_id, current_flow_step, status, 
                                   status_updated_at, station_changed_at)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        Instant now = Instant.now();
        template.update(sql,
            orderId,
            menuItem.getId(),
            1, // current_flow_step по умолчанию
            OrderItemStationStatus.ADDED.name(),
            Timestamp.from(now),
            Timestamp.from(now)
        );
    }

    /**
     * Получает все активные заказы для сборщика (аналогично getAllActiveCollectorOrdersWithItems)
     */
    public List<OrderShortDto> getAllActiveCollectorOrdersWithItems(City city) {
        // Для сборщика используем те же активные заказы
        return getAllActiveOrdersWithItems(city);
    }

    /**
     * Устанавливает приоритетное время для заказа (сразу после первого активного заказа)
     */
    public void setPriorityForOrderAfterCooking(City city, Long orderId) {
        // Получаем все активные заказы, исключая текущий
        List<OrderShortDto> activeOrders = getAllActiveOrdersWithItems(city).stream()
                .filter(order -> !order.getId().equals(orderId))
                .sorted(Comparator.comparing(OrderShortDto::getKitchenShouldGetOrderAt))
                .collect(Collectors.toList());

        if (activeOrders.isEmpty()) {
            // Если нет заказов - добавляем 1 минуту к текущему времени
            Instant priorityTime = Instant.now().plusSeconds(60);
            updateKitchenShouldGetOrderAt(city, orderId, priorityTime);
            return;
        }

        OrderShortDto firstOrder = activeOrders.get(0);
        Instant priorityTime;

        if (activeOrders.size() >= 2) {
            // Есть второй заказ - находим середину между ними
            OrderShortDto secondOrder = activeOrders.get(1);
            Instant firstTime = firstOrder.getKitchenShouldGetOrderAt();
            Instant secondTime = secondOrder.getKitchenShouldGetOrderAt();

            // Вычисляем разницу и делим пополам
            long timeDiff = secondTime.getEpochSecond() - firstTime.getEpochSecond();
            priorityTime = firstTime.plusSeconds(timeDiff / 2);
        } else {
            // Нет второго заказа - добавляем 1 минуту к первому
            priorityTime = firstOrder.getKitchenShouldGetOrderAt().plusSeconds(65);
        }

        updateKitchenShouldGetOrderAt(city, orderId, priorityTime);
    }

    /**
     * Возвращает позиции заказа (сбрасывает их на первый шаг)
     */
    public void returnOrderItems(City city, Long orderId, List<Long> orderItemIds) {
        JdbcTemplate template = getTemplate(city);

        if (orderItemIds == null || orderItemIds.isEmpty()) {
            return;
        }

        Instant now = Instant.now();

        // Обновляем позиции заказа - сбрасываем на первый шаг
        String updateItemsSql = """
            UPDATE order_item 
            SET current_flow_step = ?, status = ?, status_updated_at = ?, station_changed_at = ?
            WHERE id = ANY(?)
            """;

        // PostgreSQL использует массив для ANY
        String arrayPlaceholder = "ARRAY[" + orderItemIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",")) + "]";

        String sql = updateItemsSql.replace("?", arrayPlaceholder);
        // Используем более простой подход
        for (Long itemId : orderItemIds) {
            template.update("""
                UPDATE order_item 
                SET current_flow_step = ?, status = ?, status_updated_at = ?, station_changed_at = ?
                WHERE id = ?
                """,
                1, // первый шаг
                OrderItemStationStatus.ADDED.name(),
                Timestamp.from(now),
                Timestamp.from(now),
                itemId
            );
        }

        // Обновляем статус заказа на CREATED
        template.update("""
            UPDATE orders 
            SET status = ?
            WHERE id = ?
            """,
            OrderStatus.CREATED.name(),
            orderId
        );
    }

    private OrderShortDto buildOrderShortDto(java.sql.ResultSet rs, List<OrderItemDto> orderItems) throws java.sql.SQLException {
        OrderAddressDto address = null;
        if (rs.getString("address_street") != null || rs.getString("address_city") != null || rs.getString("address_house") != null) {
            address = OrderAddressDto.builder()
                    .street(rs.getString("address_street"))
                    .flat(rs.getString("address_flat"))
                    .floor(rs.getString("address_floor"))
                    .entrance(rs.getString("address_entrance"))
                    .comment(rs.getString("address_comment"))
                    .city(rs.getString("address_city"))
                    .doorphone(rs.getString("address_doorphone"))
                    .house(rs.getString("address_house"))
                    .build();
        }

        OrderType orderType = null;
        String orderTypeStr = rs.getString("order_type");
        if (orderTypeStr != null) {
            try {
                orderType = OrderType.valueOf(orderTypeStr);
            } catch (IllegalArgumentException ignored) {}
        }

        PaymentType paymentType = null;
        String paymentTypeStr = rs.getString("payment_type");
        if (paymentTypeStr != null) {
            try {
                paymentType = PaymentType.valueOf(paymentTypeStr);
            } catch (IllegalArgumentException ignored) {}
        }

        return OrderShortDto.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name") + (rs.getBoolean("preorder") ? "[предзаказ]" : ""))
                .status(OrderStatus.valueOf(rs.getString("status")))
                .items(orderItems)
                .shouldBeFinishedAt(rs.getTimestamp("should_be_finished_at") != null ?
                    rs.getTimestamp("should_be_finished_at").toInstant() : null)
                .kitchenShouldGetOrderAt(rs.getTimestamp("kitchen_should_get_order_at") != null ?
                    rs.getTimestamp("kitchen_should_get_order_at").toInstant() : null)
                .kitchenGotOrderAt(rs.getTimestamp("kitchen_got_order_at") != null ?
                    rs.getTimestamp("kitchen_got_order_at").toInstant() : null)
                .orderType(orderType)
                .address(address)
                .customerPhone(rs.getString("customer_phone"))
                .paymentType(paymentType)
                .deliveryTime(rs.getTimestamp("delivery_time") != null ?
                    rs.getTimestamp("delivery_time").toInstant() : null)
                .build();
    }

    private JdbcTemplate getTemplate(City city) {
        return switch (city) {
            case PARNAS -> parnasJdbcTemplate;
            case UKHTA -> ukhtaJdbcTemplate;
        };
    }
}

