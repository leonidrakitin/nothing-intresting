package ru.sushi.delivery.kds.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.persist.entity.Screen;
import ru.sushi.delivery.kds.domain.persist.entity.Station;
import ru.sushi.delivery.kds.domain.persist.holder.ScreenHolder;
import ru.sushi.delivery.kds.domain.persist.holder.StationHolder;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StationService {

    private final StationHolder stationHolder;

    public Collection<Station> getAll() {
        return stationHolder.findAll();
    }
}
