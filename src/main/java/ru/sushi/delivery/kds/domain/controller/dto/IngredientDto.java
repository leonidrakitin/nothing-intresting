package ru.sushi.delivery.kds.domain.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ru.sushi.delivery.kds.domain.persist.entity.product.Ingredient;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class IngredientDto extends AbstractProductData {

    private Long pieceInGrams;

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
