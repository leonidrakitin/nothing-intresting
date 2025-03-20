package ru.sushi.delivery.kds.domain.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.sushi.delivery.kds.domain.controller.dto.SourceDto;
import ru.sushi.delivery.kds.domain.service.SourceItemService;

import java.util.List;

@RequestMapping("/sources")
@RestController
@RequiredArgsConstructor
public class SourceController {

    private final SourceItemService sourceItemService;

    @GetMapping("/all")
    public List<SourceDto> getAllSources() {
        return this.sourceItemService.getAllSources();
    }

    @GetMapping("/prepacks")
    public List<SourceDto> getAllPrepacks() {
        return this.sourceItemService.getAllPrepacks();
    }

    @GetMapping("/ingredients")
    public List<SourceDto> getAllIngredients() {
        return this.sourceItemService.getAllIngredients();
    }
}
