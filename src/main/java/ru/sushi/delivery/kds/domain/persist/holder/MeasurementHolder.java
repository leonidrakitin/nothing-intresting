package ru.sushi.delivery.kds.domain.persist.holder;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import ru.sushi.delivery.kds.domain.persist.entity.Measurement;
import ru.sushi.delivery.kds.domain.util.BusinessLogic;

import java.util.List;

@Component
public class MeasurementHolder extends AbstractInMemoryHolder<Measurement, Long> {
    @PostConstruct
    public void init() {
        load(List.of(
                BusinessLogic.MEASUREMENT_AMOUNT,
                BusinessLogic.MEASUREMENT_GRAMS
        ));
    }
}