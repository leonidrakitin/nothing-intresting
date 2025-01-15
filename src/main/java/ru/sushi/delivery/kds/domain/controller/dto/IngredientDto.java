package ru.sushi.delivery.kds.domain.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sushi.delivery.kds.domain.persist.entity.product.Ingredient;

import java.time.Duration;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngredientDto {

    private Long id;

    private String name;

    private Long pieceInGrams;

    private String measurementUnitName;

    private Duration expirationDuration;

    private double notifyAfterAmount;

    public static IngredientDto of(Ingredient ingredient) {
        return IngredientDto.builder()
                .id(ingredient.getId())
                .name(ingredient.getName())
                .pieceInGrams(ingredient.getPieceInGrams())
                .measurementUnitName(ingredient.getMeasurementUnit().getName())
                .expirationDuration(ingredient.getExpirationDuration())
                .notifyAfterAmount(ingredient.getNotifyAfterAmount())
                .build();
    }
}
