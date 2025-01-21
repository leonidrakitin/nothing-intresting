package ru.sushi.delivery.kds.domain.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sushi.delivery.kds.domain.persist.entity.product.IngredientItem;
import ru.sushi.delivery.kds.model.DiscontinuedReason;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngredientItemData {

    private Long id;
    private String ingredientName;
    private String sourceType;
    private Double amount;
    private Instant expirationDate;
    private Instant discontinuedAt;
    private String discontinuedComment;
    private DiscontinuedReason discontinuedReason;
    private Instant updatedAt;
    private Instant createdAt;
    private String updatedBy;
    private String createdBy;

    public static IngredientItemData of(IngredientItem ingredientItem) {
        return IngredientItemData.builder()
                .id(ingredientItem.getId())
                .sourceType(ingredientItem.getSourceType().name())
                .amount(ingredientItem.getAmount())
                .expirationDate(ingredientItem.getExpirationDate())
                .discontinuedAt(ingredientItem.getDiscontinuedAt())
                .discontinuedComment(ingredientItem.getDiscontinuedComment())
                .discontinuedReason(ingredientItem.getDiscontinuedReason())
                .updatedAt(ingredientItem.getUpdatedAt())
                .createdAt(ingredientItem.getCreatedAt())
                .updatedBy(ingredientItem.getUpdatedBy())
                .createdBy(ingredientItem.getCreatedBy())
                .ingredientName(ingredientItem.getIngredient().getName())
                .build();
    }
}
