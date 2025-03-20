package ru.sushi.delivery.kds.domain.controller.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sushi.delivery.kds.model.DiscontinuedReason;
import ru.sushi.delivery.kds.model.SourceType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WriteOffRequest {

    private Long id;
    private SourceType sourceType;
    private String employeeName;
    private Double writeOffAmount;
    private DiscontinuedReason discontinuedReason;
    private String customReasonComment;
}
