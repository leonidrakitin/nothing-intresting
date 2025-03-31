package ru.sushi.delivery.kds.domain.persist.entity.product;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import ru.sushi.delivery.kds.domain.persist.entity.Measurement;

import java.time.Duration;
import java.time.Instant;

@SuperBuilder(toBuilder = true)
@Getter
@Setter
@MappedSuperclass
@NoArgsConstructor
@AllArgsConstructor
public abstract class Product {

    private String name;

    private Double fcPrice;

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne
    @JoinColumn(name = "measurement_unit_id")
    private Measurement measurementUnit;

    private Duration expirationDuration;

    private double notifyAfterAmount;

    private Instant updatedAt;

    private String updatedBy;

    private Instant createdAt;

    private String createdBy;

    public abstract Long getId();

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
