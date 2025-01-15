package ru.sushi.delivery.kds.domain.service;

import com.vaadin.flow.router.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.controller.dto.PrepackDto;
import ru.sushi.delivery.kds.domain.persist.entity.Measurement;
import ru.sushi.delivery.kds.domain.persist.entity.product.Prepack;
import ru.sushi.delivery.kds.domain.persist.repository.MeasurementRepository;
import ru.sushi.delivery.kds.domain.persist.repository.product.PrepackRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PrepackService {

    private final PrepackRepository prepackRepository;
    private final MeasurementRepository measurementRepository;

    public Prepack get(Long id) {
        return this.prepackRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Prepack not found id " + id));
    }

    public List<PrepackDto> getAllPrepacks() {
        return this.prepackRepository.findAll().stream().map(PrepackDto::of).toList();
    }

    public void savePrepack(PrepackDto prepackDTO) {
        Measurement measurement = measurementRepository.findByName(prepackDTO.getMeasurementUnitName())
                .orElseThrow(() -> new IllegalArgumentException("Invalid measurement unit"));

        Prepack prepack = new Prepack();
        prepack.setName(prepackDTO.getName());
        prepack.setExpirationDuration(prepackDTO.getExpirationDuration());
        prepack.setNotifyAfterAmount(prepackDTO.getNotifyAfterAmount());
        prepack.setMeasurementUnit(measurement);

        prepackRepository.save(prepack);
    }
}
