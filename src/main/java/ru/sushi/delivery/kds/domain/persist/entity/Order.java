package ru.sushi.delivery.kds.domain.persist.entity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
import ru.sushi.delivery.kds.model.OrderStatus;
import ru.sushi.delivery.kds.model.OrderType;
import ru.sushi.delivery.kds.model.PaymentType;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Audited
@Entity
@Table(name = "orders")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor(force = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Order {

    @Id
    @SequenceGenerator(name = "orders_id_seq_gen", sequenceName = "orders_id_generator", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orders_id_seq_gen")
    private Long id;

    private String name;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.CREATED;

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    private Instant shouldBeFinishedAt;

    private Instant kitchenShouldGetOrderAt;

    private Instant kitchenGotOrderAt;

    @Builder.Default
    private Instant statusUpdateAt = ZonedDateTime.now().toInstant();

    @Builder.Default
    private Instant createdAt = ZonedDateTime.now().toInstant();

    @Builder.Default
    private Boolean preorder = false;

    @Enumerated(EnumType.STRING)
    private OrderType orderType;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "street", column = @Column(name = "address_street")),
        @AttributeOverride(name = "flat", column = @Column(name = "address_flat")),
        @AttributeOverride(name = "floor", column = @Column(name = "address_floor")),
        @AttributeOverride(name = "entrance", column = @Column(name = "address_entrance")),
        @AttributeOverride(name = "comment", column = @Column(name = "address_comment")),
        @AttributeOverride(name = "city", column = @Column(name = "address_city")),
        @AttributeOverride(name = "doorphone", column = @Column(name = "address_doorphone")),
        @AttributeOverride(name = "house", column = @Column(name = "address_house")),
        @AttributeOverride(name = "latitude", column = @Column(name = "address_lat")),
        @AttributeOverride(name = "longitude", column = @Column(name = "address_lon"))
    })
    private OrderAddress address;

    private String customerPhone;

    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;

    private Instant deliveryTime; // Время доставки (только для доставки)

    /** ID заявки в Яндекс Доставке после «Вызвать Яндекс» */
    private String yandexClaimId;

    /** Время отправки уведомления в Telegram (наш курьер) */
    private Instant telegramNotifiedAt;

    public static Order of(String name, Instant shouldBeFinishedAt, Instant kitchenShouldGetOrderAt) {
        return Order.builder()
                .name(name)
                .shouldBeFinishedAt(shouldBeFinishedAt)
                .kitchenShouldGetOrderAt(kitchenShouldGetOrderAt)
                .build();
    }

    public static Order of(Long id) {
        return Order.builder().id(id).build();
    }
}
