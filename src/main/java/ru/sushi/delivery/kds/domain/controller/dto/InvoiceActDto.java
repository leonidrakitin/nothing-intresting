package ru.sushi.delivery.kds.domain.controller.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.sushi.delivery.kds.domain.persist.entity.act.InvoiceAct;
import ru.sushi.delivery.kds.dto.act.InvoiceActItemDto;

import java.time.LocalDate;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class InvoiceActDto {
    private final Long id;
    private final Long employeeId;
    private final String name;
    private final String vendor;
    private final LocalDate date;
    private final List<InvoiceActItemDto> itemDataList;

    public static InvoiceActDto of(InvoiceAct invoiceAct) {
        return new InvoiceActDto(
                invoiceAct.getId(),
                invoiceAct.getEmployeeId(),
                invoiceAct.getName(),
                invoiceAct.getVendor(),
                invoiceAct.getDate().toLocalDate(),
                invoiceAct.getInvoiceActItems().stream().map(InvoiceActItemDto::of).toList()
        );
    }
}
