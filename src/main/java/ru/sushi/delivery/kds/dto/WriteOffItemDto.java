package ru.sushi.delivery.kds.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.sushi.delivery.kds.model.DiscontinuedReason;
import ru.sushi.delivery.kds.model.SourceType;
import ru.sushi.delivery.kds.wrappers.WriteOffItemWrapper;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WriteOffItemDto {

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

    public static WriteOffItemDto of(WriteOffItemWrapper writeOffItemWrapper) {
        return WriteOffItemDto.builder()
                .id(writeOffItemWrapper.getId())
                .sourceType(writeOffItemWrapper.getSourceType())
                .sourceId(writeOffItemWrapper.getSourceId())
                .name(writeOffItemWrapper.getName())
                .amount(writeOffItemWrapper.getAmount())
                .discontinuedComment(writeOffItemWrapper.getDiscontinuedComment())
                .isCompleted(writeOffItemWrapper.getIsCompleted())
                .createdBy(writeOffItemWrapper.getCreatedBy())
                .createdAt(writeOffItemWrapper.getCreatedAt())
                .build();
    }
}
