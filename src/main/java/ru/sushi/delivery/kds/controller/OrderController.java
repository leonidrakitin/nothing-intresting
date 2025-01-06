package ru.sushi.delivery.kds.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.sushi.delivery.kds.dto.OrderItemDto;
import ru.sushi.delivery.kds.service.ViewService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
@RestController
@RequestMapping
public class OrderController {

    private final ViewService viewService;

    @GetMapping("/api/screens/{screenId}/station")
    public ResponseEntity<Map<String, Long>> getScreenStation(@PathVariable String screenId) {
        Optional<Long> stationId = viewService.getScreenStationIfExists(screenId);
        if (stationId.isPresent()) {
            return ResponseEntity.ok(Map.of("stationId", stationId.get()));
        }
        else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/api/screens/{screenId}/orders")
    public ResponseEntity<List<OrderItemDto>> getScreenOrderItems(@PathVariable String screenId) {
        return ResponseEntity.ok(viewService.getScreenOrderItems(screenId));
    }

    @PostMapping("/api/orders/{id}/updateStatus")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id) {
        viewService.updateStatus(id);
        return ResponseEntity.ok().build();
    }
}
