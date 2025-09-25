package ru.sushi.delivery.kds.config;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class CityProperties {

    private final String city;

    public CityProperties() {
        this.city = loadCityFromFile();
    }

    private String loadCityFromFile() {
        try {
            Path cityFilePath = Paths.get(".city");
            if (Files.exists(cityFilePath)) {
                String content = Files.readString(cityFilePath).trim();
                return content.isEmpty() ? "Неизвестный город" : content;
            } else {
                return "Неизвестный город";
            }
        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла .city: " + e.getMessage());
            return "Неизвестный город";
        }
    }

    public String getCity() {
        return city;
    }
}
