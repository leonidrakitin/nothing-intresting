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
    public static PrepackData of(Prepack prepack, Double totalAmount) {
        double fcPrice = prepack.getFcPrice()/totalAmount * (prepack.getMeasurementUnit().getId() == 1 ? 1000 : 100);
        return PrepackData.builder()
                .id(prepack.getId())
                .name(prepack.getName())
                .fcPrice(fcPrice)
                .measurementUnitName(prepack.getMeasurementUnit().getName())
                .expirationDuration(prepack.getExpirationDuration())
                .notifyAfterAmount(prepack.getNotifyAfterAmount())
                .build();
    }
}