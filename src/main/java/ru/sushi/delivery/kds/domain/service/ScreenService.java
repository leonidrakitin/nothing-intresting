package ru.sushi.delivery.kds.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.persist.entity.Item;
import ru.sushi.delivery.kds.domain.persist.entity.Screen;
import ru.sushi.delivery.kds.domain.persist.holder.ScreenHolder;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ScreenService {

    private final ScreenHolder screenHolder;

    public Optional<Screen> get(String id) {
        return screenHolder.get(id);
    }

    public Screen getOrThrow(String id) {
        return screenHolder.getOrThrow(id);
    }

    public Collection<Screen> getAll() {
        return screenHolder.findAll();
    }
}
