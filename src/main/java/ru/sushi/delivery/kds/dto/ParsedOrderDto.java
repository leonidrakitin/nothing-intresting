package ru.sushi.delivery.kds.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sushi.delivery.kds.domain.persist.entity.ItemCombo;
import ru.sushi.delivery.kds.domain.persist.entity.product.MenuItem;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParsedOrderDto {
    private String orderNumber;
    private String comment;
    private List<ParsedItem> items = new ArrayList<>();
    private List<ParsedCombo> combos = new ArrayList<>();
    private Map<String, Integer> extras = new java.util.HashMap<>(); // Название -> количество
    private Instant kitchenStartTime; // Может быть null
    private Instant finishTime; // Может быть null
    private Integer instrumentsCount; // Количество приборов
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParsedItem {
        private String name;
        private Integer quantity;
        private MenuItem menuItem; // Найденный MenuItem, может быть null
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParsedCombo {
        private String name;
        private Integer quantity;
        private ItemCombo combo; // Найденный ItemCombo, может быть null
    }
}

