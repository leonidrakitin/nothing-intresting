package ru.sushi.delivery.kds.domain.persist.entity.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.Audited;
import ru.sushi.delivery.kds.model.DiscontinuedReason;
import ru.sushi.delivery.kds.model.SourceType;

import java.time.Instant;

@Audited
@Entity
@Table(name = "write_off_item")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(force = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WriteOffItem {

    @Id
    @SequenceGenerator(name = "write_off_id_seq_gen", sequenceName = "write_off_id_generator", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "write_off_id_seq_gen")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private SourceType sourceType;

    @Column(name = "source_id", nullable = false)
    private Long sourceId;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "iscompleted", nullable = false)
    private Boolean isCompleted;

    @Column(name = "discontinued_comment")
    private String discontinuedComment;

    @Enumerated(EnumType.STRING)
    @Column(name = "discontinued_reason")
    private DiscontinuedReason discontinuedReason;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    public static WriteOffItem of(
        Long sourceId,
        SourceType sourceType,
            Double amount,
            Boolean isCompleted,
            String comment,
            String employeeName,
            DiscontinuedReason reason
    ) {
        return WriteOffItem.builder()
            .sourceId(sourceId)
            .sourceType(sourceType)
                .amount(amount)
                .isCompleted(isCompleted)
                .discontinuedComment(comment)
                .discontinuedReason(reason)
                .createdBy(employeeName)
                .build();
    }
}
