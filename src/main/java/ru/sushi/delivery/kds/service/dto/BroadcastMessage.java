package ru.sushi.delivery.kds.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class BroadcastMessage {
    private BroadcastMessageType type;
    private String content;

    public static BroadcastMessage of(BroadcastMessageType type) {
        return new BroadcastMessage(type, null);
    }
}
