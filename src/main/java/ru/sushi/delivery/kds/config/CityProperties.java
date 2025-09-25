package ru.sushi.delivery.kds.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "application")
public class CityProperties {

    private String city = "Неизвестный город";

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
