package ru.sushi.delivery.kds.domain.persist.entity.recipe;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import ru.sushi.delivery.kds.domain.persist.entity.Measurement;
import ru.sushi.delivery.kds.model.SourceType;

@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public abstract class Recipe {

    private Long sourceId;

    @Enumerated(EnumType.STRING)
    private SourceType sourceType;

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne
    @JoinColumn(name = "measurement_unit_id")
    private Measurement measurement;

    private Double initAmount;

    private Double finalAmount;

    @Builder.Default
    private Double lossesAmount = 0.0;

    @Builder.Default
    private Double lossesPercentage = 0.0;

    @Builder.Default
    private long priority = 1;

    public abstract Long getId();
}
