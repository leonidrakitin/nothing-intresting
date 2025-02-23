package ru.sushi.delivery.kds.task;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.sushi.delivery.kds.domain.service.OrderService;

@Component
@RequiredArgsConstructor
public class UpdateKitchenGotOrderAt {

    private final OrderService orderService;

    @Scheduled(fixedRate = 10000)
    public void performTask() {
        this.orderService.checkAndUpdateKitchenGotOrderAt();
    }

}
