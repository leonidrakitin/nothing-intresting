package ru.sushi.delivery.kds.domain.service;

import com.vaadin.flow.router.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.controller.dto.PrepackData;
import ru.sushi.delivery.kds.domain.persist.entity.Measurement;
import ru.sushi.delivery.kds.domain.persist.entity.product.Prepack;
import ru.sushi.delivery.kds.domain.persist.repository.MeasurementRepository;
import ru.sushi.delivery.kds.domain.persist.repository.product.PrepackRepository;
import ru.sushi.delivery.kds.domain.persist.repository.recipe.PrepackRecipeRepository;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.DoubleStream;

@Service
@RequiredArgsConstructor
public class PrepackService {

    private final PrepackRepository prepackRepository;
    private final PrepackRecipeRepository prepackRecipeRepository;
    private final MeasurementRepository measurementRepository;

    public Prepack get(Long id) {
        return this.prepackRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Prepack not found id " + id));
    }

    public List<PrepackData> getAllPrepacks() {
        return this.prepackRepository.findAll().stream()
                .map(prepack -> PrepackData.of(
                        prepack,
                        this.prepackRecipeRepository.findByPrepackId(prepack.getId()).stream()
                                .flatMapToDouble(recipe -> DoubleStream.of(recipe.getInitAmount()))
                                .sum()
                ))
                .toList();
    }

    public void deletePrepack(PrepackData prepackData) {
        this.prepackRepository.deleteById(prepackData.getId());
    }

    public void savePrepack(PrepackData prepackData) {
        Measurement measurement = this.measurementRepository.findByName(prepackData.getMeasurementUnitName())
                .orElseThrow(() -> new IllegalArgumentException("Invalid measurement unit"));

        Prepack prepack = Optional.ofNullable(prepackData.getId())
                .map(this.prepackRepository::findById)
                .flatMap(Function.identity())
                .map(p -> setNewPrepackData(p, prepackData, measurement))
                .orElseGet(() -> Prepack.of(prepackData, measurement));

        this.prepackRepository.save(prepack);
    }

    public Prepack setNewPrepackData(
            Prepack prepack,
            PrepackData prepackData,
            Measurement measurement
    ) {
        return prepack.toBuilder()
                .id(prepackData.getId())
                .name(prepackData.getName())
                .expirationDuration(prepackData.getExpirationDuration())
                .notifyAfterAmount(prepackData.getNotifyAfterAmount())
                .measurementUnit(measurement)
                .build();
    }
}
