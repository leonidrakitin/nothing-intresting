package ru.sushi.delivery.kds.domain.persist.entity.recipe;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import ru.sushi.delivery.kds.model.SourceType;

@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public abstract class Recipe {

    @Id
    private Long id;

    private Long sourceId;

    @Enumerated(EnumType.STRING)
    private SourceType sourceType;

    private Double initAmount;

    private Double finalAmount;

    @Builder.Default
    private Double lossesAmount = 0.0;

    @Builder.Default
    private Double lossesPercentage = 0.0;
}
