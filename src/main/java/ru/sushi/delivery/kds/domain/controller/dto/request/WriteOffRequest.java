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

    private Long sourceItemId;                 // ID продукта (IngredientItem / PrepackItem)
    private SourceType sourceType;             // INGREDIENT или PREPACK
    private String employeeName;               // имя сотрудника
    private Double writeOffAmount;
    private DiscontinuedReason discontinuedReason; // причина (SPOILED, FINISHED и т.д.)
    private String customReasonComment;        // опционально — свой комментарий
}
