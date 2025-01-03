package ru.sushi.delivery.kds.task;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.sushi.delivery.kds.service.dto.BroadcastMessage;
import ru.sushi.delivery.kds.service.dto.BroadcastMessageType;
import ru.sushi.delivery.kds.service.listeners.OrderChangesListener;

@Component
@RequiredArgsConstructor
public class UpdateChefScreenViewTask {

    private final OrderChangesListener orderChangesListener;

    @Scheduled(fixedRate = 1000)
    public void performTask() {
        this.orderChangesListener.broadcastAll(BroadcastMessage.of(BroadcastMessageType.REFRESH_PAGE));
    }
}
