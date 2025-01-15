package ru.sushi.delivery.kds.domain.persist.entity.product;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
import ru.sushi.delivery.kds.domain.controller.dto.PrepackData;
import ru.sushi.delivery.kds.domain.persist.entity.Measurement;

@Audited
@AuditOverride(forClass = Product.class)
@Entity
@Table(name = "prepack")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(force = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Prepack extends Product {

    @Id
    @SequenceGenerator(name = "prepack_id_seq_gen", sequenceName = "prepack_id_generator", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "prepack_id_seq_gen")
    private Long id;

    public static Prepack of(PrepackData prepackData, Measurement measurement) {
        return Prepack.builder()
                .name(prepackData.getName())
                .measurementUnit(measurement)
                .expirationDuration(prepackData.getExpirationDuration())
                .notifyAfterAmount(prepackData.getNotifyAfterAmount())
                .build();
    }
}
