package ru.sushi.delivery.kds.domain.persist.entity;

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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import ru.sushi.delivery.kds.domain.persist.entity.product.MenuItem;

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
    private MenuItem menuItem;

    private Integer length;

    private Integer width;

    private Integer height;
}
