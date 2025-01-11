package ru.sushi.delivery.kds.domain.persist.entity.product;

import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
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

    @Id
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "measurement_unit_id")
    private Measurement measurementUnit;

    private Duration expirationDuration;

    private double notifyAfterAmount;

    private Instant updatedAt;

    private String updatedBy;

    private Instant createdAt;

    private String createdBy;
}
