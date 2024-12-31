package ru.sushi.delivery.kds.service;

import lombok.Data;
import ru.sushi.delivery.kds.domain.Item;
import ru.sushi.delivery.kds.persist.model.ScreenSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Data
public class ChefScreenHolder {
    private final Map<Long, List<Item>> screenOrdersMap = new ConcurrentHashMap<>();
    private final Map<Long, ScreenSettings> screenSettingsMap = new ConcurrentHashMap<>();

    public List<Item> getOrdersForScreen(Long screenId) {
        if (screenId == null) {
            return new ArrayList<>();
        }
        return screenOrdersMap.getOrDefault(screenId, new ArrayList<>());
    }

    public ScreenSettings getScreenSettings(Long screenId) {
        if (screenId == null) {
            return null;
        }
        return screenSettingsMap.getOrDefault(screenId, null);
    }

    public Item getScreenOrder(Long screenId, Long orderId) {
        return Optional.ofNullable(screenOrdersMap.get(screenId))
                .flatMap(s -> s.stream().filter(o -> o.getId() == orderId).findFirst())
                .orElse(null);
    }

    public Long createNewSession() {
        Long newId = ThreadLocalRandom.current().nextLong();
        screenOrdersMap.put(newId, new ArrayList<>());
        screenSettingsMap.put(newId, new ScreenSettings());
        return newId;
    }
}
