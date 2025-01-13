package ru.sushi.delivery.kds.domain.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.sushi.delivery.kds.domain.controller.dto.ProcessingActDto;
import ru.sushi.delivery.kds.domain.service.ActService;
import ru.sushi.delivery.kds.domain.service.RecipeService;
import ru.sushi.delivery.kds.dto.PrepackRecipeItemDto;

import java.util.List;

@RequestMapping("/processing")
@RestController
@RequiredArgsConstructor
public class ProcessingController {

    private final ActService actService;
    private final RecipeService recipeService;

    @GetMapping("/recipe/{prepackId}")
    public List<PrepackRecipeItemDto> getPrepackRecipe(final long prepackId) {
        return this.recipeService.getPrepackRecipe(prepackId);
    }

    @PostMapping("/save")
    public void saveProcessing(@Validated @RequestBody final ProcessingActDto request) {
        this.actService.createProcessingAct(request);
    }

    //todo do not forget remove processing act items + prepack items
    @PostMapping("/delete/{processingActId}")
    public void saveProcessing(final long processingActId) {
//        this.actService.deleteProcessingAct(processingActId);
    }
}
