package ru.sushi.delivery.kds.domain.persist.entity.product;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.AuditOverride;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import ru.sushi.delivery.kds.domain.controller.dto.ProcessingActDto;
import ru.sushi.delivery.kds.domain.persist.entity.act.InvoiceActItem;
import ru.sushi.delivery.kds.domain.persist.entity.act.ProcessingAct;
import ru.sushi.delivery.kds.dto.act.InvoiceActItemDto;
import ru.sushi.delivery.kds.model.SourceType;

import java.time.Instant;

@Audited
@AuditOverride(forClass = SourceItem.class)
@Entity
@Table(name = "prepack_item")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(force = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PrepackItem extends SourceItem {

    @Id
    @SequenceGenerator(name = "prepack_item_id_seq_gen", sequenceName = "prepack_item_id_generator", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "prepack_item_id_seq_gen")
    private Long id;

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne
    @JoinColumn(name = "prepack_id")
    private Prepack prepack;

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processing_act_id")
    private ProcessingAct processingAct;

    public static PrepackItem of(Prepack prepack, InvoiceActItemDto item, InvoiceActItem itemAct) {
        return PrepackItem.builder()
                .invoiceActItem(itemAct)
                .sourceType(SourceType.PREPACK)
                .amount(itemAct.getAmount())
                .barcode(item.getBarcode())
                .expirationDate(Instant.now().plus(prepack.getExpirationDuration()))
                .prepack(prepack)
                .build();
    }

    public static PrepackItem of(Prepack prepack, ProcessingActDto actDto, ProcessingAct act) {
        return PrepackItem.builder()
                .processingAct(act)
                .sourceType(SourceType.PREPACK)
                .amount(act.getAmount())
                .barcode(actDto.getBarcode())
                .expirationDate(Instant.now().plus(prepack.getExpirationDuration()))
                .prepack(prepack)
                .build();
    }
}
