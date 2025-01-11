package ru.sushi.delivery.kds.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.persist.entity.flow.Station;
import ru.sushi.delivery.kds.domain.persist.repository.flow.StationRepository;
import ru.sushi.delivery.kds.model.OrderStatus;

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

    public Station getByOrderStatus(OrderStatus status){
        return stationRepository.findByOrderStatusAtStation(status);
    }
}
