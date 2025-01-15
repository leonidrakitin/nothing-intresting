package ru.sushi.delivery.kds.domain.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sushi.delivery.kds.domain.persist.entity.product.Prepack;

import java.time.Duration;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrepackDto {

    private Long id;

    private String name;

    private String measurementUnitName;

    private Duration expirationDuration;

    private double notifyAfterAmount;

    public static PrepackDto of(Prepack prepack) {
        return PrepackDto.builder()
                .id(prepack.getId())
                .name(prepack.getName())
                .measurementUnitName(prepack.getMeasurementUnit().getName())
                .expirationDuration(prepack.getExpirationDuration())
                .notifyAfterAmount(prepack.getNotifyAfterAmount())
                .build();
    }
}
