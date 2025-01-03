package ru.sushi.delivery.kds.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.persist.entity.Station;
import ru.sushi.delivery.kds.domain.persist.repository.StationRepository;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class StationService {

    private final StationRepository stationRepository;

    public Collection<Station> getAll() {
        return stationRepository.findAll();
    }
    
    public Station getById(Long id){
        return stationRepository.findById(id).orElseThrow();
    }
}
