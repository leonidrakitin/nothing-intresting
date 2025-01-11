package ru.sushi.delivery.kds.dto.act;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.sushi.delivery.kds.model.SourceType;

@Getter
@RequiredArgsConstructor
public class ProcessingSourceItemDto {
    private final Long sourceId;
    private final SourceType sourceType;
    private final Double finalAmount;
}
