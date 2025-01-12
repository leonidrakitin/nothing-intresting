package ru.sushi.delivery.kds.domain.persist.entity.act;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.AuditOverride;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import ru.sushi.delivery.kds.domain.controller.dto.ProcessingActDto;
import ru.sushi.delivery.kds.domain.persist.entity.product.Prepack;

import java.util.ArrayList;
import java.util.List;

@Audited
@AuditOverride(forClass = Act.class)
@Entity
@Table(name = "processing_act")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(force = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProcessingAct extends Act {

    @Id
    @SequenceGenerator(
            name = "processing_act_id_seq_gen",
            sequenceName = "processing_act_id_generator",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "processing_act_id_seq_gen")
    private Long id;

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne
    @JoinColumn(name = "prepack_id")
    private Prepack prepack;

    private Double amount;

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @OneToMany(mappedBy = "processingAct", fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProcessingSourceItem> processingSourceItems = new ArrayList<>();

    public static ProcessingAct of(Prepack prepack, ProcessingActDto processingData) {
        return ProcessingAct.builder()
                .employeeId(processingData.getEmployeeId())
                .name(processingData.getName())
                .prepack(prepack)
                .amount(processingData.getAmount())
                .build();
    }
}
