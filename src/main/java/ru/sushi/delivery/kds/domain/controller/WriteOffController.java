package ru.sushi.delivery.kds.domain.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import ru.sushi.delivery.kds.domain.controller.dto.request.WriteOffRequest;
import ru.sushi.delivery.kds.domain.service.WriteOffService;
import ru.sushi.delivery.kds.dto.WriteOffItemDto;

@RequestMapping
@RestController
@RequiredArgsConstructor
public class WriteOffController {

    private final WriteOffService writeOffService;

    @PostMapping("/v1/write-off")
    public void writeOff(@RequestBody WriteOffRequest writeOffRequest) {
        writeOffService.writeOff(writeOffRequest);
    }

    @PostMapping("/v2/write-off")
    public void addWriteOff(@RequestBody WriteOffRequest writeOffRequest) {
        writeOffService.writeOffV2(writeOffRequest);
    }

    @GetMapping("/write-off/list")
    public Page<WriteOffItemDto> getAllWriteOff(@RequestParam Integer p, Integer e) {
        PageRequest pageRequest = PageRequest.of(p, e, Sort.by(Sort.Direction.DESC, "id"));
        return writeOffService.getAll(pageRequest);
    }
}
