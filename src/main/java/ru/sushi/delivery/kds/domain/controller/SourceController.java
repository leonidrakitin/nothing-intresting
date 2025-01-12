package ru.sushi.delivery.kds.domain.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.sushi.delivery.kds.domain.controller.dto.SourceDto;
import ru.sushi.delivery.kds.domain.service.SourceService;

import java.util.List;

@RequestMapping("/sources")
@RestController
@RequiredArgsConstructor
public class SourceController {

    private final SourceService sourceService;

    @GetMapping
    public List<SourceDto> getAllSources() {
        return this.sourceService.getAllSources();
    }
}
