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
import ru.sushi.delivery.kds.dto.act.ProcessingSourceItemDto;
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

    public static ProcessingSourceItem of(ProcessingAct processingAct, ProcessingSourceItemDto item) {
        return ProcessingSourceItem.builder()
                .processingAct(processingAct)
                .sourceId(item.getSourceId())
                .sourceType(item.getSourceType())
                .finalAmount(item.getFinalAmount())
                .build();
    }
}
