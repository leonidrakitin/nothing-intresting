package ru.sushi.delivery.kds.domain.persist.entity.product;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
import org.hibernate.envers.RelationTargetAuditMode;
import ru.sushi.delivery.kds.dto.act.InvoiceActItemDto;
import ru.sushi.delivery.kds.model.SourceType;

import java.time.Instant;

@Audited
@AuditOverride(forClass = SourceItem.class)
@Entity
@Table(name = "ingredient_item")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(force = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class IngredientItem extends SourceItem {

    @Id
    @SequenceGenerator(name = "ingredient_item_id_seq_gen", sequenceName = "ingredient_item_id_generator", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ingredient_item_id_seq_gen")
    private Long id;

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    public static IngredientItem of(Ingredient ingredient, InvoiceActItemDto item) {
        return IngredientItem.builder()
                .sourceType(SourceType.INGREDIENT)
                .amount(item.getAmount())
                .barcode(item.getBarcode())
                .expirationDate(Instant.now().plus(ingredient.getExpirationDuration()))
                .ingredient(ingredient)
                .build();
    }
}
