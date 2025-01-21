package ru.sushi.delivery.kds.domain.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sushi.delivery.kds.domain.persist.entity.product.PrepackItem;
import ru.sushi.delivery.kds.model.DiscontinuedReason;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrepackItemData {

    private Long id;
    private String prepackName;
    private String sourceType;                 // тип источника (PREPACK)
    private Long barcode;                          // штрих-код
    private Double amount;                         // текущее количество
    private Instant expirationDate;                // дата истечения срока годности
    private Instant discontinuedAt;                // дата списания/прекращения использования
    private String discontinuedComment;            // комментарий к списанию
    private DiscontinuedReason discontinuedReason; // причина списания (если есть)
    private Instant updatedAt;
    private Instant createdAt;
    private String updatedBy;
    private String createdBy;

    /**
     * Метод-конвертер из сущности PrepackItem в DTO
     */
    public static PrepackItemData of(PrepackItem entity) {
        if (entity == null) {
            return null;
        }
        return PrepackItemData.builder()
                .id(entity.getId())
                .sourceType(entity.getSourceType().name())
                .barcode(entity.getBarcode())
                .amount(entity.getAmount())
                .expirationDate(entity.getExpirationDate())
                .discontinuedAt(entity.getDiscontinuedAt())
                .discontinuedComment(entity.getDiscontinuedComment())
                .discontinuedReason(entity.getDiscontinuedReason())
                .updatedAt(entity.getUpdatedAt())
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .createdBy(entity.getCreatedBy())
                .prepackName(entity.getPrepack().getName())
                .build();
    }
}
