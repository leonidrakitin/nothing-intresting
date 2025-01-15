package ru.sushi.delivery.kds.domain.controller.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.sushi.delivery.kds.domain.persist.entity.act.ProcessingAct;

import java.time.LocalDate;

@Getter
@RequiredArgsConstructor
public class ProcessingActInfoResponse {

    private final Long id;
    private final Long prepackId;
    private final String prepackName;
    private final Double amount;
    private final String measurementUnit;
    private final String name = "N/A";
    private final String employeeName;
    private final LocalDate date;

    public static ProcessingActInfoResponse of(String employeeName, ProcessingAct processingAct) {
        return new ProcessingActInfoResponse(
                processingAct.getId(),
                processingAct.getPrepack().getId(),
                processingAct.getPrepack().getName(),
                processingAct.getAmount(),
                processingAct.getPrepack().getMeasurementUnit().getName(),
                employeeName,
                processingAct.getDate().toLocalDate()
        );
    }
}
