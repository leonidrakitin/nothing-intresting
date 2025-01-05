package ru.sushi.delivery.kds.domain.persist.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
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
    @JoinColumn(name = "item_id")
    private Item item;

    @Builder.Default
    private Integer currentFlowStep = 1;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private OrderItemStationStatus status = OrderItemStationStatus.ADDED;

    @Builder.Default
    private Instant statusUpdatedAt = Instant.now();

    @Builder.Default
    private Instant stationChangedAt = Instant.now();

    public static OrderItem of(Order order, Item item) {
        return OrderItem.builder()
                .order(order)
                .item(item)
                .build();
    }
}
