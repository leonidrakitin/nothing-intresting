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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import ru.sushi.delivery.kds.domain.controller.dto.MenuItemData;
import ru.sushi.delivery.kds.domain.persist.entity.flow.Flow;

import java.sql.Time;

@Audited
@Entity
@Table(name = "menu_item")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor(force = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MenuItem {

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

    private Time timeToCook;

    public static MenuItem of(MenuItemData menuItemData, Flow flow) {
        return MenuItem.builder()
                .name(menuItemData.getName())
                .flow(flow)
                .timeToCook(menuItemData.getTimeToCook())
                .build();
    }
}
