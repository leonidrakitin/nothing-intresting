package ru.sushi.delivery.kds.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.persist.entity.flow.Screen;
import ru.sushi.delivery.kds.domain.persist.repository.flow.ScreenRepository;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ScreenService {

    private final ScreenRepository screenRepository;

    public Optional<Screen> get(String id) {
        return screenRepository.findById(id);
    }

    public Screen getOrThrow(String id) {
        return screenRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Screen not found"));
    }

    public Collection<Screen> getAll() {
        return screenRepository.findAll();
    }
}
