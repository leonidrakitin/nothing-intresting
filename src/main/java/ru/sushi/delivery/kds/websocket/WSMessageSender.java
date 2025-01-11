package ru.sushi.delivery.kds.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import ru.sushi.delivery.kds.model.WSMessageType;
import ru.sushi.delivery.kds.websocket.dto.WSNotification;
import ru.sushi.delivery.kds.websocket.dto.WSRefresh;

@RequiredArgsConstructor
@Component
public class WSMessageSender {

    private final SimpMessagingTemplate template;

    public void sendNotification(Long screenId, String payload) {
        template.convertAndSend(
            "/topic/screen.notification/" + screenId,
            new WSNotification(WSMessageType.NOTIFICATION, payload)
        );
    }

    public void sendRefreshAll() {
        template.convertAndSend(
            "/topic/screen.refresh",
            new WSRefresh(WSMessageType.REFRESH)
        );
    }
}
