package ru.sushi.delivery.kds.domain.persist.holder;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import ru.sushi.delivery.kds.domain.persist.entity.Station;
import ru.sushi.delivery.kds.domain.util.BusinessLogic;

import java.util.List;

@Component
public class StationHolder extends AbstractInMemoryHolder<Station, Long> {
    @PostConstruct
    public void init() {
        load(List.of(
                BusinessLogic.STATION_TYPE_DEFAULT,
                BusinessLogic.STATION_TYPE_COLD,
                BusinessLogic.STATION_TYPE_HOT,
                BusinessLogic.STATION_TYPE_COLLECT
        ));
    }
}