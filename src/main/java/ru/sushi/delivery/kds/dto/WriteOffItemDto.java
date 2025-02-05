package ru.sushi.delivery.kds.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.sushi.delivery.kds.domain.persist.entity.product.WriteOffItem;
import ru.sushi.delivery.kds.model.DiscontinuedReason;
import ru.sushi.delivery.kds.model.SourceType;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WriteOffItemDto {

    private Long id;
    private SourceType sourceType;
    private String name;
    private Long warehouseId;
    private Double amount;
    private String discontinuedComment;
    private DiscontinuedReason discontinuedReason;
    private Boolean isCompleted;
    private String createdBy;
    private Instant createdAt;

    public static WriteOffItemDto of(WriteOffItem writeOffItem, String name) {
        return WriteOffItemDto.builder()
                .id(writeOffItem.getId())
                .sourceType(writeOffItem.getSourceType())
                .warehouseId(writeOffItem.getProductId())
                .name(name)
                .amount(writeOffItem.getAmount())
                .discontinuedComment(writeOffItem.getDiscontinuedComment())
                .isCompleted(writeOffItem.getIsCompleted())
                .createdBy(writeOffItem.getCreatedBy())
                .createdAt(writeOffItem.getCreatedAt())
                .build();
    }
}
