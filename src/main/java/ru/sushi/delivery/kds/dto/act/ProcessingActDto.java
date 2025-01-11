package ru.sushi.delivery.kds.dto.act;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class ProcessingActDto {
    private final Long employeeId;
    private final Long prepackId;
    private final Double amount;
    private final Long barcode;
    private final String name;
    private final List<ProcessingSourceItemDto> itemDataList;
}
