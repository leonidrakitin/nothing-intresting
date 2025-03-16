package ru.sushi.delivery.kds.dto.act;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Builder(toBuilder = true)
public class InvoiceActItemDto {

    private final Long id;

    private final String name;

    @PositiveOrZero
    private final Long sourceId;

    private final String sourceType;

    @PositiveOrZero
    private final Double amount;

    @PositiveOrZero
    private final Double price;

    private final Long barcode;
}
