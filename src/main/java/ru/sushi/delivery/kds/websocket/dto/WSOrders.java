package ru.sushi.delivery.kds.websocket.dto;

import ru.sushi.delivery.kds.dto.OrderFullDto;
import ru.sushi.delivery.kds.model.WSMessageType;

import java.util.List;


public record WSOrders(WSMessageType type, List<OrderFullDto> payload) {

}
