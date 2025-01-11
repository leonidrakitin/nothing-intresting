package ru.sushi.delivery.kds.dto.act;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class InvoiceActDto {
    private final Long employeeId;
    private final String name;
    private final List<InvoiceActItemDto> itemDataList;
}
