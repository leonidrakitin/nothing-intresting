package ru.sushi.delivery.kds.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.persist.entity.Measurement;
import ru.sushi.delivery.kds.domain.persist.repository.MeasurementRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MeasurementService {

    private final MeasurementRepository measurementRepository;

    public List<Measurement> getAll() {
        return measurementRepository.findAll();
    }
}
