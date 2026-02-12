package ru.sushi.delivery.kds.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

/**
 * Генерирует статичное изображение карты (Яндекс Static API) с наложенным номером заказа.
 * Использует сохранённые координаты адреса (latitude/longitude), геокодер не вызывается.
 */
@Log4j2
@Service
public class StaticMapImageService {

    private static final int MAP_WIDTH = 450;
    private static final int MAP_HEIGHT = 450;
    private static final int LABEL_FONT_SIZE = 32;
    private static final int LABEL_PADDING = 12;
    private static final int CAPTION_MAX_LENGTH = 1024;

    @Value("${yandex.geocoder.apikey:}")
    private String geocoderApikey;

    private final java.net.http.HttpClient httpClient = java.net.http.HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * Строит URL для Яндекс Static API (v1). ll, pt — долгота,широта (longitude,latitude).
     * Apikey обязателен, иначе 403.
     */
    private String buildStaticMapUrl(double longitude, double latitude) {
        if (geocoderApikey == null || geocoderApikey.isBlank()) {
            log.debug("Static map: yandex.geocoder.apikey не задан, картинка не будет запрошена");
            return null;
        }
        String ll = longitude + "," + latitude;
        String pt = ll + ",pm2rdm";
        return "https://static-maps.yandex.ru/v1?ll=" + URLEncoder.encode(ll, StandardCharsets.UTF_8)
                + "&size=" + MAP_WIDTH + "," + MAP_HEIGHT
                + "&z=16&l=map&pt=" + URLEncoder.encode(pt, StandardCharsets.UTF_8)
                + "&apikey=" + URLEncoder.encode(geocoderApikey, StandardCharsets.UTF_8);
    }

    /**
     * Генерирует PNG: статичная карта по сохранённым координатам + номер заказа поверх.
     *
     * @param longitude долгота (из адреса заказа)
     * @param latitude  широта (из адреса заказа)
     * @param orderName номер заказа (например "П-123")
     * @return PNG-байты или empty при ошибке
     */
    public Optional<byte[]> buildMapImageWithOrderNumber(double longitude, double latitude, String orderName) {
        try {
            String mapUrl = buildStaticMapUrl(longitude, latitude);
            if (mapUrl == null) {
                return Optional.empty();
            }
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(URI.create(mapUrl))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();
            java.net.http.HttpResponse<byte[]> response = httpClient.send(
                    request,
                    java.net.http.HttpResponse.BodyHandlers.ofByteArray()
            );
            if (response.statusCode() != 200) {
                log.warn("Static map request failed: {} {}", response.statusCode(), mapUrl);
                return Optional.empty();
            }
            byte[] mapBytes = response.body();
            if (mapBytes == null || mapBytes.length == 0) {
                return Optional.empty();
            }
            BufferedImage mapImage = ImageIO.read(new java.io.ByteArrayInputStream(mapBytes));
            if (mapImage == null) {
                log.warn("Failed to decode static map image");
                return Optional.empty();
            }
            BufferedImage out = new BufferedImage(mapImage.getWidth(), mapImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = out.createGraphics();
            g.drawImage(mapImage, 0, 0, null);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            String label = orderName != null && !orderName.isBlank() ? orderName : "?";
            Font font = new Font(Font.SANS_SERIF, Font.BOLD, LABEL_FONT_SIZE);
            g.setFont(font);
            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(label);
            int textHeight = fm.getAscent();
            int boxWidth = textWidth + LABEL_PADDING * 2;
            int boxHeight = textHeight + LABEL_PADDING * 2;
            int x = 10;
            int y = 10;
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRoundRect(x, y, boxWidth, boxHeight, 8, 8);
            g.setColor(Color.WHITE);
            g.drawString(label, x + LABEL_PADDING, y + LABEL_PADDING + fm.getAscent() - fm.getDescent());
            g.dispose();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(out, "PNG", baos);
            return Optional.of(baos.toByteArray());
        } catch (Exception e) {
            log.warn("Failed to build map image for order {}: {}", orderName, e.getMessage());
            return Optional.empty();
        }
    }

    /** Максимальная длина подписи к фото в Telegram. */
    public static int getCaptionMaxLength() {
        return CAPTION_MAX_LENGTH;
    }
}
