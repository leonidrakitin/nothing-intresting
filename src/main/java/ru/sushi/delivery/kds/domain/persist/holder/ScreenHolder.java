package ru.sushi.delivery.kds.domain.persist.holder;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import ru.sushi.delivery.kds.domain.persist.entity.OrderItem;
import ru.sushi.delivery.kds.domain.persist.entity.Screen;
import ru.sushi.delivery.kds.domain.util.BusinessLogic;

import java.util.List;

@Component
public class ScreenHolder extends AbstractInMemoryHolder<Screen, String> {
    @PostConstruct
    public void init() {
        load(List.of(
                BusinessLogic.COLD_SCREEN,
                BusinessLogic.HOT_SCREEN,
                BusinessLogic.COLLECT_SCREEN
        ));
    }
}