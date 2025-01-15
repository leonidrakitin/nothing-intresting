package ru.sushi.delivery.kds.domain.controller.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@RequiredArgsConstructor
public class GetProcessingRequest {

    @PositiveOrZero
    private final int pageNumber = 0;

    @Max(200)
    private final int pageSize = 100;

    private final String vendorFilter;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private final LocalDate fromDate;

    private final String fieldSort = "id";

    private final String sortDirection = "asc";
}
