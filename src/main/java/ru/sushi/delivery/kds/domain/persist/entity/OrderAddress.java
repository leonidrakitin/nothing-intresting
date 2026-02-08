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
@Builder
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
}
