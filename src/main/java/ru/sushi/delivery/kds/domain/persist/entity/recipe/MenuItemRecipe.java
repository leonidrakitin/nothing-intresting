package ru.sushi.delivery.kds.domain.persist.entity.recipe;

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
import ru.sushi.delivery.kds.domain.persist.entity.product.MenuItem;

@Audited
@AuditOverride(forClass = Recipe.class)
@Entity
@Table(name = "recipe_menu_item")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(force = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MenuItemRecipe extends Recipe {

    @Id
    @SequenceGenerator(name = "menu_item_recipe_id_seq_gen", sequenceName = "menu_item_recipe_id_generator", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "menu_item_recipe_id_seq_gen")
    private Long id;

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne
    @JoinColumn(name = "menu_item_id")
    private MenuItem menuItem;

    /**
     * <p>ID станции, на которой становится виден ингридиент.</p>
     */
    private Long stationId;
}
