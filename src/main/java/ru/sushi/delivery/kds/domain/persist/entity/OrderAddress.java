package ru.sushi.delivery.kds.domain.persist.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class OrderAddress {
    private String street;
    private String flat;
    private String floor;
    private String entrance;
    private String comment;
    private String city;
    private String doorphone;
    private String house;
    /** Широта (результат геокодирования при создании заказа). */
    private Double latitude;
    /** Долгота (результат геокодирования при создании заказа). */
    private Double longitude;
}
