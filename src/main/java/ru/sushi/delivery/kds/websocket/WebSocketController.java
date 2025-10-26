package ru.sushi.delivery.kds.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import ru.sushi.delivery.kds.model.WSMessageType;
import ru.sushi.delivery.kds.service.ViewService;
import ru.sushi.delivery.kds.websocket.dto.WSOrders;

@RequiredArgsConstructor
@Controller
public class WebSocketController {

    private final ViewService viewService;
//    private final Map<WSMessageType, AbstractWSMessageHandler> handlerMap;

    @MessageMapping("/topic/screen.getAllOrders/{screenId}")
    @SendTo("/topic/screen.orders/{screenId}")
    public WSOrders getAllStationOrders(@DestinationVariable("screenId") Long screenId) {
        return new WSOrders(WSMessageType.GET_ALL_ORDER_ITEMS, viewService.getScreenOrderItems(screenId));
    }

    @MessageMapping("/topic/screen.getAllOrdersWithItems/{screenId}")
    @SendTo("/topic/screen.orders/{screenId}")
    public WSOrders getAllOrdersWithItems(@DestinationVariable("screenId") String screenId) {
        return new WSOrders(WSMessageType.GET_ALL_ORDERS, viewService.getAllKitchenOrdersWithItems());
    }


    @MessageMapping("/topic/screen/{screenId}/update.orderItem/{orderItemId}")
    @SendTo("/topic/screen.orders/{screenId}")
    public WSOrders updateOrder(
        @DestinationVariable("screenId") Long screenId,
        @DestinationVariable("orderItemId") Long orderItemId
    ) {
        viewService.updateStatus(orderItemId);
        return new WSOrders(WSMessageType.GET_ALL_ORDER_ITEMS, viewService.getScreenOrderItems(screenId));
    }

    @MessageMapping("/topic/screen/{screenId}/update.allOrder.done/{orderId}")
    @SendTo("/topic/screen.orders/{screenId}")
    public WSOrders updateAllToDone(
        @DestinationVariable("orderId") Long orderId
    ) {
        viewService.updateAllOrderItemsToDone(orderId);
        return new WSOrders(WSMessageType.GET_ALL_ORDERS, viewService.getAllKitchenOrdersWithItems());
    }

    @MessageMapping("/topic/screen/{screenId}/update.order.done/{orderItemId}")
    @SendTo("/topic/screen.orders/{screenId}")
    public WSOrders updateOrderToDone(
        @DestinationVariable("orderItemId") Long orderItemId
    ) {
        viewService.updateStatus(orderItemId);
        return new WSOrders(WSMessageType.GET_ALL_ORDERS, viewService.getAllKitchenOrdersWithItems());
    }
}
