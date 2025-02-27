package ru.sushi.delivery.kds.domain.persist.entity.act;

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
import ru.sushi.delivery.kds.domain.controller.dto.PrepackRecipeItemDto;
import ru.sushi.delivery.kds.model.SourceType;

@Audited
@Entity
@Table(name = "processing_act_item")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor(force = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProcessingSourceItem {

    @Id
    @SequenceGenerator(
            name = "processing_act_item_id_seq_gen",
            sequenceName = "processing_act_item_id_generator",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "processing_act_item_id_seq_gen")
    private Long id;

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne
    @JoinColumn(name = "processing_id")
    private ProcessingAct processingAct;

    private Long sourceId;

    @Enumerated(EnumType.STRING)
    private SourceType sourceType;

    private Double finalAmount;

    private Double initAmount;

    private Double lossesAmount;

    private Double lossesPercentage;

    public static ProcessingSourceItem of(
            SourceType sourceType,
            ProcessingAct processingAct,
            PrepackRecipeItemDto item
    ) {
        return ProcessingSourceItem.builder()
                .processingAct(processingAct)
                .sourceId(item.getSourceId())
                .sourceType(sourceType)
                .finalAmount(item.getFinalAmount())
                .initAmount(item.getInitAmount()) //todo add new fields !!!!audit + table
                .lossesAmount(item.getLossesAmount()) //todo add new fields !!!!audit + table
                .lossesPercentage(item.getLossesPercentage()) //todo add new fields !!!!audit + table
                .finalAmount(item.getFinalAmount())
                .build();
    }
}
