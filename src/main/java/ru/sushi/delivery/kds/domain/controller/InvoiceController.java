package ru.sushi.delivery.kds.domain.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.sushi.delivery.kds.domain.controller.dto.InvoiceActDto;
import ru.sushi.delivery.kds.domain.controller.dto.request.GetInvoicesRequest;
import ru.sushi.delivery.kds.domain.controller.dto.response.GetInvoicesResponse;
import ru.sushi.delivery.kds.domain.service.ActService;

import java.util.List;

@RequestMapping("/invoices")
@RestController
@RequiredArgsConstructor
public class InvoiceController {

    private final ActService actService;

    @GetMapping
    public List<GetInvoicesResponse> getInvoices(@Validated final GetInvoicesRequest request) {
        return this.actService.getAllInvoices(request);
    }

    @GetMapping("/{invoiceId}")
    public InvoiceActDto getInvoice(@PathVariable final Long invoiceId) {
        return this.actService.getInvoice(invoiceId);
    }

    @PostMapping("/save")
    public void saveInvoice(@Validated @RequestBody final InvoiceActDto request) {
        this.actService.saveInvoiceAct(request);
    }
}
