package ru.sushi.delivery.kds.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderTimelineDto {
    private String orderName;
    private String endPoint;
    private String status;
}
