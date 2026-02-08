package ru.sushi.delivery.kds.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportOrderResponse {
    private Long orderId;
    private String orderName;
    private String city;
}
