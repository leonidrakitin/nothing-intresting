package ru.sushi.delivery.kds.task;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.sushi.delivery.kds.service.ChefScreenOrderChangesListener;

@Component
public class UpdateChefScreenViewTask {

    @Scheduled(fixedRate = 1000)
    public void performTask() {
        ChefScreenOrderChangesListener.broadcast("$timer");
    }
}
