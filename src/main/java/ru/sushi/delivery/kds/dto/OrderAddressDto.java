package ru.sushi.delivery.kds.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sushi.delivery.kds.domain.persist.entity.OrderAddress;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class OrderAddressDto {
    private String street;
    private String flat;
    private String floor;
    private String entrance;
    private String comment;
    private String city;
    private String doorphone;
    private String house;
    /** Широта (сохранённый результат геокодирования). */
    private Double latitude;
    /** Долгота (сохранённый результат геокодирования). */
    private Double longitude;

    /** Строка адреса для геокодирования и отображения (город, улица, дом, кв., этаж, подъезд, домофон). */
    public String toFullAddressString() {
        StringBuilder sb = new StringBuilder();
        if (city != null && !city.isBlank()) sb.append(city);
        if (street != null && !street.isBlank()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(street);
        }
        if (house != null && !house.isBlank()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(house);
        }
        if (flat != null && !flat.isBlank()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append("кв. ").append(flat);
        }
        if (floor != null && !floor.isBlank()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append("эт. ").append(floor);
        }
        if (entrance != null && !entrance.isBlank()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append("под. ").append(entrance);
        }
        if (doorphone != null && !doorphone.isBlank()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append("домофон ").append(doorphone);
        }
        return sb.length() > 0 ? sb.toString() : "Адрес не указан";
    }

    public static OrderAddressDto of(OrderAddress address) {
        if (address == null) {
            return null;
        }
        return OrderAddressDto.builder()
                .street(address.getStreet())
                .flat(address.getFlat())
                .floor(address.getFloor())
                .entrance(address.getEntrance())
                .comment(address.getComment())
                .city(address.getCity())
                .doorphone(address.getDoorphone())
                .house(address.getHouse())
                .latitude(address.getLatitude())
                .longitude(address.getLongitude())
                .build();
    }

    public OrderAddress toEntity() {
        return OrderAddress.builder()
                .street(street)
                .flat(flat)
                .floor(floor)
                .entrance(entrance)
                .comment(comment)
                .city(city)
                .doorphone(doorphone)
                .house(house)
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }
}
