package ru.sushi.delivery.kds.domain.persist.entity.recipe;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.AuditOverride;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import ru.sushi.delivery.kds.domain.controller.dto.MealRecipeDto;
import ru.sushi.delivery.kds.domain.controller.dto.SourceDto;
import ru.sushi.delivery.kds.domain.persist.entity.Measurement;
import ru.sushi.delivery.kds.domain.persist.entity.product.Meal;
import ru.sushi.delivery.kds.model.SourceType;

@Audited
@AuditOverride(forClass = Recipe.class)
@Entity
@Table(name = "recipe_menu_item")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(force = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MealRecipe extends Recipe {

    @Id
    @SequenceGenerator(name = "menu_item_recipe_id_seq_gen", sequenceName = "menu_item_recipe_id_generator", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "menu_item_recipe_id_seq_gen")
    private Long id;

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne
    @JoinColumn(name = "menu_item_id")
    private Meal meal;

    /**
     * <p>ID станции, на которой становится виден ингридиент.</p>
     */
    private Long stationId;

    public static MealRecipe of(
            MealRecipeDto recipeData,
            SourceDto sourceDto,
            Meal meal,
            Measurement measurement
    ) {
        return MealRecipe.builder()
                .meal(meal)
                .sourceId(sourceDto.getId())
                .sourceType(SourceType.valueOf(sourceDto.getType()))
                .stationId(recipeData.getStationId())
                .initAmount(recipeData.getInitAmount())
                .finalAmount(recipeData.getFinalAmount())
                .lossesAmount(recipeData.getLossesAmount())
                .lossesPercentage(recipeData.getLossesPercentage())
                .measurement(measurement)
                .build();
    }
}
