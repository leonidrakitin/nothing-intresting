package ru.sushi.delivery.kds.domain.persist.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sushi.delivery.kds.model.RecipeSourceType;

@Entity
@Table(name = "recipe_item")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor(force = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemRecipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sourceId;

    @Enumerated(EnumType.STRING)
    private RecipeSourceType sourceType;

    private Long itemId;

    /**
     * <p>ID станции, на которой становится виден ингридиент.</p>
     */
    private Long stationId;

    private Double initAmount;

    private Double finalAmount;

    @Builder.Default
    private Double lossesAmount = 0.0;

    @Builder.Default
    private Double lossesPercentage = 0.0;
}
