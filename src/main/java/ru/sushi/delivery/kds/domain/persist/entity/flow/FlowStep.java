package ru.sushi.delivery.kds.domain.persist.entity.flow;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import ru.sushi.delivery.kds.model.FlowStepType;

@Entity
@Table(name = "flow_step")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor(force = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FlowStep {

    @Id
    @SequenceGenerator(name = "flow_step_id_seq_gen", sequenceName = "flow_step_id_generator", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "flow_step_id_seq_gen")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "flow_id")
    private Flow flow;

    @ManyToOne
    @JoinColumn(name = "station_id")
    private Station station;

    @Enumerated(EnumType.STRING)
    private FlowStepType stepType;

    private int stepOrder;
}
