package ru.sushi.delivery.kds.domain.persist.entity.product;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.AuditOverride;
import org.hibernate.envers.Audited;
import ru.sushi.delivery.kds.domain.controller.dto.IngredientDto;
import ru.sushi.delivery.kds.domain.persist.entity.Measurement;

@Audited
@AuditOverride(forClass = Product.class)
@Entity
@Table(name = "ingredient")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(force = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Ingredient extends Product {

    @Id
    @SequenceGenerator(name = "ingredient_id_seq_gen", sequenceName = "ingredient_id_generator", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ingredient_id_seq_gen")
    private Long id;

    private Long pieceInGrams;

    public static Ingredient of(IngredientDto ingredientData, Measurement measurement) {
        return Ingredient.builder()
                .pieceInGrams(ingredientData.getPieceInGrams())
                .name(ingredientData.getName())
                .measurementUnit(measurement)
                .expirationDuration(ingredientData.getExpirationDuration())
                .notifyAfterAmount(ingredientData.getNotifyAfterAmount())
                .build();
    }
}
