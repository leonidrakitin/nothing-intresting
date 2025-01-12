package ru.sushi.delivery.kds.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.sushi.delivery.kds.service.ViewService;

import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/screen/")
public class ScreenController {

    private final ViewService viewService;

    @GetMapping("{screenId}/station")
    public ResponseEntity<Map<String, Long>> getScreenStation(@PathVariable Long screenId) {
        Optional<Long> stationId = viewService.getScreenStationIfExists(screenId);
        if (stationId.isPresent()) {
            return ResponseEntity.ok(Map.of("stationId", stationId.get()));
        }
        else {
            return ResponseEntity.notFound().build();
        }
    }
}
