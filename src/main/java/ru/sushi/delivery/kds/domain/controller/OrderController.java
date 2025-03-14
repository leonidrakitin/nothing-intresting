package ru.sushi.delivery.kds.domain.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.sushi.delivery.kds.dto.OrderShortDto;
import ru.sushi.delivery.kds.service.ViewService;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/orders/")
public class OrderController {

    private final ViewService viewService; //do not use view

    @GetMapping("{screenId}")
    public ResponseEntity<List<OrderShortDto>> getScreenOrderItems(@PathVariable Long screenId) {
        return ResponseEntity.ok(viewService.getScreenOrderItems(screenId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<OrderShortDto>> getAllOrdersWithItems() {
        return ResponseEntity.ok(viewService.getAllOrdersWithItems());
    }

    @PostMapping("{orderItemId}/updateStatus")
    public ResponseEntity<Void> updateStatus(@PathVariable Long orderItemId) {
        viewService.updateStatus(orderItemId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("{orderId}/updateAllToDone")
    public ResponseEntity<Void> updateAllToDone(@PathVariable Long orderId) {
        viewService.updateAllOrderItemsToDone(orderId);
        return ResponseEntity.ok().build();
    }
}
