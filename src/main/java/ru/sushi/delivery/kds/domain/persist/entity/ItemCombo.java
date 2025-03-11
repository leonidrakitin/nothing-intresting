package ru.sushi.delivery.kds.domain.persist.entity;

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
import org.hibernate.envers.NotAudited;
import ru.sushi.delivery.kds.domain.persist.entity.product.MenuItem;

import java.util.List;

@Audited
@Entity
@Table(name = "item_combo")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor(force = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemCombo {

    @Id
    @SequenceGenerator(name = "item_combo_id_seq_gen", sequenceName = "item_combo_id_seq_generator", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "item_combo_id_seq_gen")
    private final Long id;

    private final String name;

    @NotAudited
    @ManyToMany
    @JoinTable(
            name = "item_combo_compound",
            joinColumns = @JoinColumn(name = "item_combo_id"),
            inverseJoinColumns = @JoinColumn(name = "menu_item_id")
    )
    private final List<MenuItem> menuItems;
}
