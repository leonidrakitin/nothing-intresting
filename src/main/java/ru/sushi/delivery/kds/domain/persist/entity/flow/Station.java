package ru.sushi.delivery.kds.domain.persist.entity.flow;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sushi.delivery.kds.model.OrderStatus;

@Entity
@Table(name = "station")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor(force = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Station {

    @Id
    @SequenceGenerator(name = "station_id_seq_gen", sequenceName = "station_id_generator", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "station_id_seq_gen")
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatusAtStation;
}
