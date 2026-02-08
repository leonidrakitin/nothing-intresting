package ru.sushi.delivery.kds.domain.persist.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.sushi.delivery.kds.dto.OrderTimelineDto;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderTimelineRepository {

    private static final String SQL = """
        WITH RECURSIVE daily_orders AS (
            SELECT
                o.id,
                o.name,
                o.status as order_status,
                o.kitchen_got_order_at,
                o.kitchen_should_get_order_at as received_at,
                o.should_be_finished_at as deadline,
                COUNT(oi.id) as items_count,
                CASE
                    WHEN COUNT(oi.id) = COUNT(CASE WHEN oi.status = 'COMPLETED' THEN 1 END)
                        THEN MAX(oi.status_updated_at)
                    ELSE NULL
                    END as actual_finished_at,
                COUNT(oi.id) * 2.0 as est_duration,
                ROW_NUMBER() OVER (ORDER BY o.kitchen_should_get_order_at ASC) as rn
            FROM orders o
            JOIN order_item oi ON o.id = oi.order_id
            WHERE o.kitchen_should_get_order_at::DATE = CURRENT_DATE
              AND oi.menu_item_id IN (SELECT id FROM menu_item WHERE product_type_id IN (1, 2, 3, 4, 5, 6, 10, 11))
            GROUP BY o.id, o.name, o.status, o.kitchen_got_order_at, o.kitchen_should_get_order_at, o.should_be_finished_at
        ),
        timeline AS (
            SELECT
                rn, name, order_status, kitchen_got_order_at, received_at, deadline, actual_finished_at, est_duration,
                COALESCE(actual_finished_at, received_at + (est_duration * INTERVAL '1 minute')) as end_point
            FROM daily_orders WHERE rn = 1

            UNION ALL

            SELECT
                d.rn, d.name, d.order_status, d.kitchen_got_order_at, d.received_at, d.deadline, d.actual_finished_at, d.est_duration,
                CASE
                    WHEN d.actual_finished_at IS NOT NULL THEN d.actual_finished_at
                    ELSE (GREATEST(d.received_at, t.end_point) + (d.est_duration * INTERVAL '1 minute'))
                    END as end_point
            FROM daily_orders d
            JOIN timeline t ON d.rn = t.rn + 1
        )
        SELECT
            name as "orderName",
            TO_CHAR(end_point, 'HH24:MI') as "end_point",
            CASE
                WHEN order_status = 'CANCELED' THEN 'ОТМЕНЕН'
                WHEN actual_finished_at IS NOT NULL OR order_status = 'READY' THEN 'ГОТОВ'
                WHEN order_status = 'COLLECTING' THEN 'СОБИРАЕМ'
                WHEN kitchen_got_order_at IS NULL THEN 'ЕЩЕ НЕ НАЧАЛИ ГОТОВИТЬ'
                ELSE 'ГОТОВИМ'
            END as "status"
        FROM timeline
        WHERE name = ?
        """;

    private final JdbcTemplate jdbcTemplate;

    public Optional<OrderTimelineDto> findTimelineByOrderName(String orderName) {
        List<OrderTimelineDto> results = jdbcTemplate.query(SQL, (rs, rowNum) -> new OrderTimelineDto(
                rs.getString("orderName"),
                rs.getString("end_point"),
                rs.getString("status")
        ), orderName);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
}
