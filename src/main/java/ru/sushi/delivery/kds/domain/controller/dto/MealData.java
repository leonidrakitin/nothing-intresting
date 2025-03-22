package ru.sushi.delivery.kds.domain.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sushi.delivery.kds.domain.persist.entity.product.Meal;

import java.time.Duration;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealData {

    private String flow;

    private Double price;

    private Long id;

    private String name;

    private Duration timeToCook;

    public static MealData of(Meal meal) {
        return MealData.builder()
                .flow(meal.getFlow().getName())
                .id(meal.getId())
                .name(meal.getName())
                .price(meal.getPrice())
                .timeToCook(meal.getTimeToCook())
                .build();
    }
}
