package ru.sushi.delivery.kds.domain.controller.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ru.sushi.delivery.kds.domain.persist.entity.product.Prepack;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@SuperBuilder
public class PrepackData extends AbstractProductData {

    public static PrepackData of(Prepack prepack) {
        return PrepackData.builder()
                .id(prepack.getId())
                .name(prepack.getName())
                .fcPrice(prepack.getFcPrice())
                .measurementUnitName(prepack.getMeasurementUnit().getName())
                .expirationDuration(prepack.getExpirationDuration())
                .notifyAfterAmount(prepack.getNotifyAfterAmount())
                .build();
    }
}