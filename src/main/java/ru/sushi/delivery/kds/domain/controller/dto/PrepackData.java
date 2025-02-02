package ru.sushi.delivery.kds.domain.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ru.sushi.delivery.kds.domain.persist.entity.product.Prepack;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PrepackData extends AbstractProductData {

    public static PrepackData of(Prepack prepack) {
        return PrepackData.builder()
                .id(prepack.getId())
                .name(prepack.getName())
                .measurementUnitName(prepack.getMeasurementUnit().getName())
                .expirationDuration(prepack.getExpirationDuration())
                .notifyAfterAmount(prepack.getNotifyAfterAmount())
                .build();
    }
}