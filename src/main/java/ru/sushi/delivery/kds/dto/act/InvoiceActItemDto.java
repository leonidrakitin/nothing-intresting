package ru.sushi.delivery.kds.dto.act;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.sushi.delivery.kds.model.SourceType;

@Getter
@RequiredArgsConstructor
public class InvoiceActItemDto {
    private Long sourceId;
    private SourceType sourceType;
    private Double amount;
    private Double price;
    private Long barcode;
}
