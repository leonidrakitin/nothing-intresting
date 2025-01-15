package ru.sushi.delivery.kds.domain.controller.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class ProcessingActDto {

    private final long id;
    private final Long employeeId = 1L;
    private final Long prepackId;
    private final Double amount; //final amount of prepack
    private final Long barcode; //null
    private final String name; //null
    private final List<PrepackRecipeItemDto> itemDataList;
}
