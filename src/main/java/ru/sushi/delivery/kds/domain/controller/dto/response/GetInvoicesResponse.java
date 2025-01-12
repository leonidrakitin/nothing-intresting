package ru.sushi.delivery.kds.domain.controller.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.sushi.delivery.kds.domain.persist.entity.act.InvoiceAct;
import ru.sushi.delivery.kds.domain.persist.entity.act.InvoiceActItem;

import java.time.LocalDate;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class GetInvoicesResponse {
    private final Long id;
    private final String vendor;
    private final LocalDate date;
    private final Double totalCost;
    private final long totalItems;

    public static GetInvoicesResponse of(InvoiceAct invoiceAct) {
        List<InvoiceActItem> invoiceActItems = invoiceAct.getInvoiceActItems();
        Double totalCost = invoiceActItems.stream().mapToDouble(InvoiceActItem::getPrice).sum();
        int totalSize = invoiceActItems.size();
        return new GetInvoicesResponse(
                invoiceAct.getId(),
                invoiceAct.getVendor(),
                invoiceAct.getDate().toLocalDate(),
                totalCost,
                totalSize
        );
    }
}
