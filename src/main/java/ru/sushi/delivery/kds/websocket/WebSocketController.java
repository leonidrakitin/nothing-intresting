package ru.sushi.delivery.kds.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

@Controller
public class WebSocketController {

    private final SimpMessagingTemplate template;

    @Autowired
    public WebSocketController(SimpMessagingTemplate template) {
        this.template = template;
    }

    // Вызываем где-то в коде:
    public void broadcastRefreshPage(Long screenId) {
        // Сообщение: { type: "REFRESH_PAGE", content: "" }
        Map<String, String> msg = new HashMap<>();
        msg.put("type", "REFRESH_PAGE");
        msg.put("content", "");
        template.convertAndSend("/topic/screen/" + screenId, msg);
    }

    public void broadcastNotification(Long screenId, String content) {
        // Сообщение: { type: "NOTIFICATION", content: "..." }
        Map<String, String> msg = new HashMap<>();
        msg.put("type", "NOTIFICATION");
        msg.put("content", content);
        template.convertAndSend("/topic/screen/" + screenId, msg);
    }
}
