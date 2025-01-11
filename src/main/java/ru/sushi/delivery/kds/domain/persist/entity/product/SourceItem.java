package ru.sushi.delivery.kds.domain.persist.entity.product;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import ru.sushi.delivery.kds.model.DiscontinuedReason;
import ru.sushi.delivery.kds.model.SourceType;

import java.time.Instant;

@SuperBuilder(toBuilder = true)
@Getter
@Setter
@MappedSuperclass
@NoArgsConstructor
@AllArgsConstructor
public abstract class SourceItem {

    @Id
    private Long id;

    private SourceType sourceType;

    private Long barcode;

    private Double amount;

    private Instant expirationDate;

    private Instant discontinuedAt;

    private String discontinuedComment;

    private DiscontinuedReason discontinuedReason;

    @Builder.Default
    private Instant updatedAt = Instant.now();

    @Builder.Default
    private Instant createdAt = Instant.now();

    private String updatedBy;

    private String createdBy;
}
