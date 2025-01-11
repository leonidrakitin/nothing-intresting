package ru.sushi.delivery.kds.domain.persist.entity.act;

import jakarta.persistence.Entity;
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
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import ru.sushi.delivery.kds.dto.act.InvoiceActDto;

import java.util.ArrayList;
import java.util.List;

@Audited
@Entity
@Table(name = "invoice_act")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(force = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class InvoiceAct extends Act {

    @Id
    @SequenceGenerator(name = "invoice_act_id_seq_gen", sequenceName = "invoice_act_id_generator", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "invoice_act_id_seq_gen")
    private Long id;

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @OneToMany(mappedBy = "invoiceAct", fetch = FetchType.LAZY)
    @Builder.Default
    private List<InvoiceActItem> invoiceActItems = new ArrayList<>();

    public static InvoiceAct of(InvoiceActDto invoiceData) {
        return InvoiceAct.builder()
                .employeeId(invoiceData.getEmployeeId())
                .name(invoiceData.getName())
                .build();
    }
}
