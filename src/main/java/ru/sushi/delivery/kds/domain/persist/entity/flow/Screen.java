package ru.sushi.delivery.kds.domain.persist.entity.flow;

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

@Entity
@Table(name = "screen")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor(force = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Screen {

    @Id
    @SequenceGenerator(name = "screen_id_seq_gen", sequenceName = "screen_id_generator", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "screen_id_seq_gen")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "station_id")
    private Station station;

    public static Screen of(Station station) {
        return Screen.builder()
                .station(station)
                .build();
    }
}
