package ru.sushi.delivery.kds.task;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.sushi.delivery.kds.domain.service.OrderService;

import java.time.Instant;
import java.time.ZonedDateTime;

@Component
@RequiredArgsConstructor
public class UpdateKitchenGotOrderAt {

    private final OrderService orderService;

    @Scheduled(fixedRate = 2000)
    public void performTask() {
        this.orderService.checkAndUpdateKitchenGotOrderAt(ZonedDateTime.now().toInstant());
    }

}
