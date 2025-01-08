package ru.sushi.delivery.kds.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import ru.sushi.delivery.kds.model.WSMessageType;
import ru.sushi.delivery.kds.service.ViewService;
import ru.sushi.delivery.kds.websocket.dto.WSOrderItems;

@RequiredArgsConstructor
@Controller
public class WebSocketController {

    private final ViewService viewService;
//    private final Map<WSMessageType, AbstractWSMessageHandler> handlerMap;

    @MessageMapping("/topic/screen.getAllOrders/{screenId}")
    @SendTo("/topic/screen.orders/{screenId}")
    public WSOrderItems getAllOrders(@DestinationVariable("screenId") Long screenId) {
        return new WSOrderItems(WSMessageType.GET_ALL_ORDER_ITEMS, viewService.getScreenOrderItems(screenId));
    }

    @MessageMapping("/topic/screen/{screenId}/update.orderItem/{orderItemId}")
    @SendTo("/topic/screen.orders/{screenId}")
    public WSOrderItems updateOrder(
        @DestinationVariable("screenId") Long screenId,
        @DestinationVariable("orderItemId") Long orderItemId
    ) {
        viewService.updateStatus(orderItemId);
        return new WSOrderItems(WSMessageType.GET_ALL_ORDER_ITEMS, viewService.getScreenOrderItems(screenId));
    }

    @MessageMapping("/topic/screen/{screenId}/update.order.done/{orderId}")
    public WSOrderItems updateAllToDone(
        @DestinationVariable("orderId") Long screenId,
        @DestinationVariable("orderId") Long orderId
    ) {
        viewService.updateAllOrderItemsToDone(orderId);
        return new WSOrderItems(WSMessageType.GET_ALL_ORDER_ITEMS, viewService.getScreenOrderItems(screenId));
    }
}
