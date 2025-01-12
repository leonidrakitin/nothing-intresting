package ru.sushi.delivery.kds.websocket.dto;

import ru.sushi.delivery.kds.dto.OrderItemDto;
import ru.sushi.delivery.kds.model.WSMessageType;

import java.util.List;


public record WSOrderItems(WSMessageType type, List<OrderItemDto> payload) {

}
