package ru.sushi.delivery.kds.domain.persist.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import ru.sushi.delivery.kds.domain.persist.entity.product.Meal;
import ru.sushi.delivery.kds.model.OrderItemStationStatus;

import java.time.Instant;

@Audited
@Entity
@Table(name = "order_item")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor(force = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderItem {

    @Id
    @SequenceGenerator(name = "orders_item_id_seq_gen", sequenceName = "orders_item_id_generator", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orders_item_id_seq_gen")
    private Long id;

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne
    @JoinColumn(name = "menu_item_id")
    private Meal meal;

    @Builder.Default
    private Integer currentFlowStep = 1;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private OrderItemStationStatus status = OrderItemStationStatus.ADDED;

    @Builder.Default
    private Instant statusUpdatedAt = Instant.now();

    @Builder.Default
    private Instant stationChangedAt = Instant.now();

    public static OrderItem of(Order order, Meal meal) {
        return OrderItem.builder()
                .order(order)
                .meal(meal)
                .build();
    }
}
