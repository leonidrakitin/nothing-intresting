package ru.sushi.delivery.kds.domain.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.sushi.delivery.kds.domain.controller.dto.IngredientItemData;
import ru.sushi.delivery.kds.domain.controller.dto.PrepackItemData;
import ru.sushi.delivery.kds.domain.controller.dto.request.WriteOffRequest;
import ru.sushi.delivery.kds.domain.service.IngredientItemService;
import ru.sushi.delivery.kds.domain.service.PrepackItemService;
import ru.sushi.delivery.kds.domain.service.WriteOffService;
import ru.sushi.delivery.kds.dto.WriteOffItemDto;

import java.util.List;

@RequestMapping("/warehouse")
@RestController
@RequiredArgsConstructor
public class WarehouseController {

    private final IngredientItemService ingredientItemService;
    private final PrepackItemService prepackItemService;
    private final WriteOffService writeOffService;

    @GetMapping("/ingredients")
    public List<IngredientItemData> getAllIngredients() {
        return ingredientItemService.getGetAll();
    }

    @GetMapping("/prepacks")
    public List<PrepackItemData> getAllPrepacks() {
        return prepackItemService.getAll();
    }

    @PostMapping("/write-off")
    public void writeOff(@RequestBody WriteOffRequest writeOffRequest) {
        writeOffService.writeOff(writeOffRequest);
    }

    @PostMapping("/add-write-off")
    public void addWriteOff(@RequestBody WriteOffRequest writeOffRequest) {
        writeOffService.createWriteOff(writeOffRequest);
    }

    @GetMapping("/allWriteOff")
    public Page<WriteOffItemDto> getAllWriteOff(@RequestParam Integer p, Integer e) {
        PageRequest pageRequest = PageRequest.of(p, e, Sort.by(Sort.Direction.DESC, "id"));
        return writeOffService.getAll(pageRequest);
    }
}
