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
import ru.sushi.delivery.kds.domain.persist.entity.act.InvoiceActItem;
import ru.sushi.delivery.kds.dto.act.InvoiceActItemDto;
import ru.sushi.delivery.kds.model.SourceType;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

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

    public static IngredientItem of(Ingredient ingredient, InvoiceActItemDto itemActDto, InvoiceActItem itemAct) {
        Duration expiration = Optional.ofNullable(ingredient.getExpirationDuration())
                .orElse(Duration.ofDays(31));
        return IngredientItem.builder()
                .invoiceActItem(itemAct)
                .sourceType(SourceType.INGREDIENT)
                .amount(itemActDto.getAmount())
                .barcode(itemActDto.getBarcode())
                .expirationDate(Instant.now().plus(expiration))
                .ingredient(ingredient)
                .build();
    }
}
