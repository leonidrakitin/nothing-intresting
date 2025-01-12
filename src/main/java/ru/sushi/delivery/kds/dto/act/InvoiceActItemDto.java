package ru.sushi.delivery.kds.dto.act;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.sushi.delivery.kds.domain.persist.entity.act.InvoiceActItem;

@Getter
@RequiredArgsConstructor
public class InvoiceActItemDto {

    private final Long id;

    @PositiveOrZero
    private final Long sourceId;

    private final String sourceType;

    @PositiveOrZero
    private final Double amount;

    @PositiveOrZero
    private final Double price;

    private final Long barcode;

    public static InvoiceActItemDto of(InvoiceActItem invoiceItem) {
        return new InvoiceActItemDto(
                invoiceItem.getId(),
                invoiceItem.getSourceId(),
                invoiceItem.getSourceType().name(),
                invoiceItem.getAmount(),
                invoiceItem.getPrice(),
                null
        );
    }
}
