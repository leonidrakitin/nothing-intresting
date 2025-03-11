package ru.sushi.delivery.kds.domain.persist.entity.product;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;
import ru.sushi.delivery.kds.domain.controller.dto.MenuItemData;
import ru.sushi.delivery.kds.domain.persist.entity.flow.Flow;

import java.util.HashSet;
import java.util.Set;

@Audited
@Entity
@Table(name = "product_type")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor(force = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductType {

    @Id
    @SequenceGenerator(name = "product_type_id_seq_gen", sequenceName = "product_type_id_seq_generator", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_type_id_seq_gen")
    private Long id;

    private String name;

    private Integer priority;

    private boolean extra;

    private Integer length;

    private Integer width;

    private Integer height;

    @ManyToMany
    @JoinTable(
            name = "product_type_neighbors",
            joinColumns = @JoinColumn(name = "product_type_id"),
            inverseJoinColumns = @JoinColumn(name = "neighbor_product_type_id")
    )
    @Builder.Default
    private Set<ProductType> allowedNeighbors = new HashSet<>();

    public static ProductType of(MenuItemData menuItemData, Flow flow) {
        return ProductType.builder()
                .name(menuItemData.getName())
//                .priority(priority)
//                .length(length)
//                .width(width)
//                .height(height)
                .build();
    }
}
