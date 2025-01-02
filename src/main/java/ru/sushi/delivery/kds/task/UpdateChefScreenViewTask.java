package ru.sushi.delivery.kds.task;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.sushi.delivery.kds.domain.util.Broadcaster;

@Component
public class UpdateChefScreenViewTask {

    @Scheduled(fixedRate = 1000)
    public void performTask() {
        Broadcaster.broadcast("$timer");
    }
}
