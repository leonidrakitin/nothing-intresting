package ru.sushi.delivery.kds.wrappers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.sushi.delivery.kds.model.DiscontinuedReason;
import ru.sushi.delivery.kds.model.SourceType;

import java.time.Instant;

@Setter
@Getter
@AllArgsConstructor
public class WriteOffItemWrapper {

    private Long id;
    private SourceType sourceType;
    private String name;
    private Long sourceId;
    private Double amount;
    private String discontinuedComment;
    private DiscontinuedReason discontinuedReason;
    private Boolean isCompleted;
    private String createdBy;
    private Instant createdAt;

}
