package ru.sushi.delivery.kds.domain.persist.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import ru.sushi.delivery.kds.domain.persist.entity.product.Meal;

@Entity
@Table(name = "product_package")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor(force = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductPackage {

    @Id
    @SequenceGenerator(
            name = "product_package_id_seq_gen",
            sequenceName = "product_package_id_seq_generator",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_package_id_seq_gen")
    private Long id;

    private String name;

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne
    @JoinColumn(name = "menu_item_id")
    private Meal meal;

    private Integer length;

    private Integer width;

    private Integer height;
}
