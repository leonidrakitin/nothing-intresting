package ru.sushi.delivery.kds.domain.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.sushi.delivery.kds.domain.controller.dto.IngredientItemData;
import ru.sushi.delivery.kds.domain.controller.dto.PrepackItemData;
import ru.sushi.delivery.kds.domain.service.IngredientItemService;
import ru.sushi.delivery.kds.domain.service.PrepackItemService;

import java.util.List;

@RequestMapping("/warehouse")
@RestController
@RequiredArgsConstructor
public class WarehouseController {

    private final IngredientItemService ingredientItemService;
    private final PrepackItemService prepackItemService;

    @GetMapping("/ingredients")
    public List<IngredientItemData> getAllIngredients() {
        return ingredientItemService.getGetAll();
    }

    @GetMapping("/prepacks")
    public List<PrepackItemData> getAllPrepacks() {
        return prepackItemService.getAll();
    }
}
