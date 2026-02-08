package ru.sushi.delivery.kds.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDelayDto {
    /**
     * Минуты задержки: 0 если вовремя или опоздание &lt;= 25 минут.
     * Иначе: округлённое вверх до 10 минус 20 (26→10, 31→20, 66→50 и т.д.)
     */
    private int delayMinutes;
}
