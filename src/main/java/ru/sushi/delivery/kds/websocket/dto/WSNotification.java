package ru.sushi.delivery.kds.websocket.dto;

import ru.sushi.delivery.kds.model.WSMessageType;


public record WSNotification(WSMessageType type, String payload) {

}
