package ru.sushi.delivery.kds.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Геокодирование адресов через Yandex Geocoder API.
 * Вызывается один раз при создании заказа; координаты сохраняются в адресе.
 */
@Log4j2
@Service
@ConditionalOnProperty(name = "yandex.geocoder.apikey", matchIfMissing = false)
public class YandexGeocoderService {

    private static final String GEOCODER_URL = "https://geocode-maps.yandex.ru/1.x/";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    @Value("${yandex.geocoder.apikey:}")
    private String apikey;

    /**
     * Геокодирование адреса. Возвращает [долгота, широта] или null при ошибке/отсутствии ключа.
     */
    public double[] geocode(String address) {
        if (address == null || address.isBlank()) return null;
        if (apikey == null || apikey.isBlank()) {
            log.warn("Yandex Geocoder: не задан yandex.geocoder.apikey");
            return null;
        }
        try {
            String url = GEOCODER_URL + "?apikey=" + apikey + "&format=json&geocode="
                    + URLEncoder.encode(address, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                log.warn("Yandex Geocoder: HTTP {} for address {}", response.statusCode(), address);
                return null;
            }
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode members = root.path("response").path("GeoObjectCollection").path("featureMember");
            if (!members.isArray() || members.isEmpty()) return null;
            String pos = members.get(0).path("GeoObject").path("Point").path("pos").asText(null);
            if (pos == null) return null;
            String[] parts = pos.trim().split("\\s+");
            if (parts.length != 2) return null;
            // Yandex Geocoder 1.x возвращает "широта долгота"; для API доставки нужны [долгота, широта]
            double lat = Double.parseDouble(parts[0]);
            double lon = Double.parseDouble(parts[1]);
            return new double[]{lon, lat};
        } catch (Exception e) {
            log.warn("Yandex Geocoder failed for {}: {}", address, e.getMessage());
            return null;
        }
    }
}
