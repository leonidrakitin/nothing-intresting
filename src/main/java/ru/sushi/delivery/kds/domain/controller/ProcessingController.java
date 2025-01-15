package ru.sushi.delivery.kds.domain.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.sushi.delivery.kds.domain.controller.dto.ProcessingActDto;
import ru.sushi.delivery.kds.domain.controller.dto.request.GetProcessingRequest;
import ru.sushi.delivery.kds.domain.controller.dto.response.ProcessingActInfoResponse;
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
    public List<PrepackRecipeItemDto> getPrepackRecipe(@PathVariable final Long prepackId) {
        return this.recipeService.getPrepackRecipe(prepackId);
    }

    @GetMapping("/acts")
    public List<ProcessingActInfoResponse> getPrepackRecipe(@Validated final GetProcessingRequest request) {
        return this.actService.getAllProcessingActs(request);
    }

    @GetMapping("/acts/{prepackRecipeId}")
    public ProcessingActDto getPrepackRecipeById(@PathVariable final Long prepackRecipeId) {
        return this.actService.getProcessingAct(prepackRecipeId);
    }

    @PostMapping("/save")
    public void saveProcessing(@Validated @RequestBody final ProcessingActDto request) {
        this.actService.createProcessingAct(request);
    }

    //todo do not forget remove processing act items + prepack items
    @DeleteMapping("/{processingActId}")
    public void deleteProcessing(@PathVariable final Long processingActId) {
        this.actService.deleteProcessingAct(processingActId);
    }
}
