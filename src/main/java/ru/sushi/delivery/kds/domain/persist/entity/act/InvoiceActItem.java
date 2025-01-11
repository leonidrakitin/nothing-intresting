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
import ru.sushi.delivery.kds.dto.act.InvoiceActItemDto;
import ru.sushi.delivery.kds.model.SourceType;

@Entity
@Table(name = "invoice_act_item")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor(force = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class InvoiceActItem {

    @Id
    @SequenceGenerator(
            name = "invoice_act_item_id_seq_gen",
            sequenceName = "invoice_act_item_id_generator",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "invoice_act_item_id_seq_gen")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "invoice_id")
    private InvoiceAct invoiceAct;

    private Long sourceId;

    @Enumerated(EnumType.STRING)
    private SourceType sourceType;

    private Double amount;

    private Double price;

    public static InvoiceActItem of(InvoiceAct invoiceAct, InvoiceActItemDto item) {
        return InvoiceActItem.builder()
                .price(item.getPrice())
                .amount(item.getAmount())
                .sourceType(item.getSourceType())
                .sourceId(item.getSourceId())
                .invoiceAct(invoiceAct)
                .build();
    }
}
