package ru.sushi.delivery.kds.domain.persist.entity.product;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import ru.sushi.delivery.kds.domain.controller.dto.MealData;
import ru.sushi.delivery.kds.domain.persist.entity.flow.Flow;

import java.time.Duration;

@Audited
@Entity
@Table(name = "menu_item")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor(force = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Meal {

    @Id
    @SequenceGenerator(name = "menu_item_id_seq_gen", sequenceName = "menu_item_id_seq_generator", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "menu_item_id_seq_gen")
    private Long id;

    private String name;

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne
    @JoinColumn(name = "product_type_id")
    private ProductType productType;

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne
    @JoinColumn(name = "flow_id")
    private Flow flow;

    private Double price;

    private Duration timeToCook;

    public static Meal of(MealData mealData, Flow flow) {
        return Meal.builder()
                .name(mealData.getName())
                .flow(flow)
                .timeToCook(mealData.getTimeToCook())
                .build();
    }
}
