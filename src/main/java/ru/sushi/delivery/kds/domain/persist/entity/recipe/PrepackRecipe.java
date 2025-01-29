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
import ru.sushi.delivery.kds.domain.controller.dto.PrepackRecipeData;
import ru.sushi.delivery.kds.domain.controller.dto.SourceDto;
import ru.sushi.delivery.kds.domain.persist.entity.Measurement;
import ru.sushi.delivery.kds.domain.persist.entity.product.Prepack;
import ru.sushi.delivery.kds.model.SourceType;

@Audited
@AuditOverride(forClass = Recipe.class)
@Entity
@Table(name = "recipe_prepack")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(force = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PrepackRecipe extends Recipe {

    @Id
    @SequenceGenerator(name = "prepack_recipe_id_seq_gen", sequenceName = "prepack_recipe_id_generator", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "prepack_recipe_id_seq_gen")
    private Long id;

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne
    @JoinColumn(name = "prepack_id")
    private Prepack prepack;

    public static PrepackRecipe of(
            PrepackRecipeData recipeData, SourceDto sourceDto, Prepack prepack, Measurement measurement
    ) {
        return PrepackRecipe.builder()
                .prepack(prepack)
                .sourceId(sourceDto.getId())
                .measurement(measurement)
                .sourceType(SourceType.valueOf(sourceDto.getType()))
                .initAmount(recipeData.getInitAmount())
                .finalAmount(recipeData.getFinalAmount())
                .lossesAmount(recipeData.getLossesAmount())
                .lossesPercentage(recipeData.getLossesPercentage())
                .build();
    }
}
