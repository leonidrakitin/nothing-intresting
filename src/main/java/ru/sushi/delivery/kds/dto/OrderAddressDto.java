package ru.sushi.delivery.kds.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sushi.delivery.kds.domain.persist.entity.OrderAddress;

@Data
@Builder
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
                .build();
    }
}
