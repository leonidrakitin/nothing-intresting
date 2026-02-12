package ru.sushi.delivery.kds.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.dto.OrderAddressDto;
import ru.sushi.delivery.kds.dto.OrderShortDto;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Интеграция с API Яндекс Доставки (экспресс).
 * Документация: https://yandex.com/support/delivery-profile/ru/api/express/overview
 */
@Log4j2
@Service
@ConditionalOnProperty(name = "yandex.delivery.token", matchIfMissing = false)
public class YandexDeliveryService {

    private static final String BASE_URL = "https://b2b.taxi.yandex.net";
    private static final String OFFERS_CALCULATE = "/b2b/cargo/integration/v2/offers/calculate";
    private static final String CLAIMS_CREATE = "/b2b/cargo/integration/v2/claims/create";
    private static final String CLAIMS_INFO = "/b2b/cargo/integration/v2/claims/info";
    private static final DateTimeFormatter ISO_OFFSET = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")
            .withZone(ZoneId.systemDefault());

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    @Value("${yandex.delivery.token}")
    private String token;

    @Value("${yandex.delivery.source-address:}")
    private String sourceAddress;

    @Value("${yandex.delivery.source-comment:}")
    private String sourceComment;

    @Value("${yandex.delivery.source-phone:}")
    private String sourcePhone;

    @Value("${yandex.geocoder.apikey:}")
    private String geocoderApikey;

    private static final String GEOCODER_URL = "https://geocode-maps.yandex.ru/1.x/";

    /** Возвращает координаты адреса доставки: из сохранённых (latitude/longitude) или через геокодер. [долгота, широта]. */
    private double[] getDestCoords(OrderAddressDto address, String fullname) {
        if (address != null && address.getLatitude() != null && address.getLongitude() != null) {
            return new double[]{address.getLongitude(), address.getLatitude()};
        }
        return geocode(fullname);
    }

    /**
     * Геокодирование адреса через Yandex Geocoder API. Возвращает [долгота, широта] или null при ошибке.
     * Координаты обязательны для успешной оценки заявки в API доставки.
     */
    private double[] geocode(String address) {
        if (address == null || address.isBlank()) return null;
        if (geocoderApikey == null || geocoderApikey.isBlank()) {
            log.warn("Yandex Geocoder: не задан yandex.geocoder.apikey — координаты не будут переданы, возможна ошибка «Оценка не удалась»");
            return null;
        }
        try {
            String url = GEOCODER_URL + "?apikey=" + geocoderApikey + "&format=json&geocode=" + URLEncoder.encode(address, StandardCharsets.UTF_8);
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
            // Yandex Geocoder 1.x возвращает "широта долгота"; API доставки ждёт [долгота, широта]
            double lat = Double.parseDouble(parts[0]);
            double lon = Double.parseDouble(parts[1]);
            return new double[]{lon, lat};
        } catch (Exception e) {
            log.warn("Yandex Geocoder failed for {}: {}", address, e.getMessage());
            return null;
        }
    }

    private void addCoordinatesToRoutePoint(ObjectNode point, double[] coords) {
        if (coords != null && coords.length >= 2) {
            point.set("coordinates", objectMapper.createArrayNode().add(coords[0]).add(coords[1]));
        }
    }

    private void addCoordinatesToAddress(ObjectNode addressNode, double[] coords) {
        if (coords != null && coords.length >= 2) {
            addressNode.set("coordinates", objectMapper.createArrayNode().add(coords[0]).add(coords[1]));
        }
    }

    /**
     * Рассчитывает офферы и создаёт заявку на доставку в Яндекс.
     *
     * @param order заказ на доставку (должен содержать адрес и телефон)
     * @return идентификатор созданной заявки (claim_id) или сообщение об ошибке
     */
    public String createClaim(OrderShortDto order) {
        return createClaim(order, null);
    }

    public String createClaim(OrderShortDto order, String taxiClass) {
        log.info("Yandex createClaim: order={}", order.getName());
        if (token == null || token.isBlank()) {
            log.warn("Yandex createClaim: токен не задан");
            throw new IllegalStateException("Яндекс Доставка: не задан токен (yandex.delivery.token)");
        }
        if (sourceAddress == null || sourceAddress.isBlank()) {
            log.warn("Yandex createClaim: не задан source-address");
            throw new IllegalStateException("Яндекс Доставка: задайте адрес точки забора (yandex.delivery.source-address)");
        }
        OrderAddressDto dest = order.getAddress();
        if (dest == null) {
            throw new IllegalArgumentException("У заказа нет адреса доставки");
        }
        String destFullname = buildFullAddress(dest);
        String destPhone = order.getCustomerPhone() != null && !order.getCustomerPhone().isBlank()
                ? normalizePhone(order.getCustomerPhone())
                : null;
        if (destPhone == null) {
            throw new IllegalArgumentException("У заказа нет телефона клиента");
        }
        String sourcePhoneForContact = sourcePhone != null && !sourcePhone.isBlank()
                ? normalizePhone(sourcePhone)
                : destPhone;

        // 1) Рассчитать офферы (координаты обязательны для успешной оценки; используем сохранённые при создании заказа)
        double[] sourceCoords = geocode(sourceAddress);
        double[] destCoords = getDestCoords(dest, destFullname);
        ObjectNode offerBody = objectMapper.createObjectNode();
        ArrayNode routePoints = offerBody.putArray("route_points");
        ObjectNode srcPoint = routePoints.addObject();
        srcPoint.put("id", 1).put("fullname", sourceAddress).put("country", "Россия");
        addCoordinatesToRoutePoint(srcPoint, sourceCoords);
        ObjectNode destPoint = routePoints.addObject();
        destPoint.put("id", 2)
                .put("fullname", destFullname)
                .put("country", "Россия")
                .put("city", dest.getCity() != null ? dest.getCity() : "");
        if (dest.getStreet() != null) destPoint.put("street", dest.getStreet());
        if (dest.getHouse() != null) destPoint.put("building", dest.getHouse());
        if (dest.getFlat() != null) destPoint.put("sflat", dest.getFlat());
        if (dest.getFloor() != null) destPoint.put("sfloor", dest.getFloor());
        addAddressDetailsToOfferPoint(destPoint, dest);
        addCoordinatesToRoutePoint(destPoint, destCoords);

        ArrayNode items = offerBody.putArray("items");
        items.addObject()
                .put("quantity", 1)
                .put("pickup_point", 1)
                .put("dropoff_point", 2)
                .put("weight", 2)
                .set("size", objectMapper.createObjectNode()
                        .put("length", 0.3)
                        .put("width", 0.3)
                        .put("height", 0.2));

        ObjectNode requirements = offerBody.putObject("requirements");
        requirements.putArray("taxi_classes").add(effectiveTaxiClass(taxiClass));
        requirements.putArray("cargo_options").add("thermobag");
        requirements.put("skip_door_to_door", false);

        String offerPayload;
        try {
            String body = objectMapper.writeValueAsString(offerBody);
            log.info("Yandex createClaim offers/calculate: source={}, dest={}, bodyLength={}", sourceAddress, buildFullAddress(dest), body.length());
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + OFFERS_CALCULATE))
                    .header("Authorization", "Bearer " + token)
                    .header("Accept-Language", "ru")
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(20))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            log.info("Yandex createClaim offers/calculate response: status={}, body={}", response.statusCode(), response.body());
            if (response.statusCode() != 200) {
                log.warn("Yandex offers/calculate failed: {} {}", response.statusCode(), response.body());
                throw new RuntimeException("Яндекс Доставка: не удалось рассчитать стоимость. " + response.body());
            }
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode offers = root.get("offers");
            if (offers == null || !offers.isArray() || offers.isEmpty()) {
                log.warn("Yandex offers/calculate: нет офферов в ответе, body={}", response.body());
                throw new RuntimeException("Яндекс Доставка: нет доступных вариантов доставки по этому адресу.");
            }
            offerPayload = offers.get(0).get("payload").asText();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Yandex offers/calculate error", e);
            throw new RuntimeException("Яндекс Доставка: ошибка запроса. " + e.getMessage());
        }

        // 2) Создать заявку
        String requestId = UUID.randomUUID().toString();
        ObjectNode createBody = objectMapper.createObjectNode();
        createBody.put("offer_payload", offerPayload);
        createBody.put("comment", "Заказ " + order.getName());

        ArrayNode routePointsCreate = createBody.putArray("route_points");
        // Точка забора (ресторан)
        ObjectNode sourcePoint = routePointsCreate.addObject();
        sourcePoint.put("point_id", 1)
                .put("visit_order", 1)
                .put("type", "source");
        ObjectNode sourceAddressNode = objectMapper.createObjectNode().put("fullname", sourceAddress);
        if (sourceComment != null && !sourceComment.isBlank()) sourceAddressNode.put("comment", sourceComment);
        addCoordinatesToAddress(sourceAddressNode, sourceCoords);
        sourcePoint.set("address", sourceAddressNode);
        sourcePoint.set("contact", objectMapper.createObjectNode()
                .put("name", "Ресторан")
                .put("phone", sourcePhoneForContact));

        // Точка доставки (клиент)
        ObjectNode destPointCreate = routePointsCreate.addObject();
        destPointCreate.put("point_id", 2)
                .put("visit_order", 2)
                .put("type", "destination")
                .put("external_order_id", order.getName());
        ObjectNode destAddress = objectMapper.createObjectNode().put("fullname", destFullname);
        if (dest.getCity() != null) destAddress.put("city", dest.getCity());
        if (dest.getStreet() != null) destAddress.put("street", dest.getStreet());
        if (dest.getHouse() != null) destAddress.put("building", dest.getHouse());
        if (dest.getComment() != null && !dest.getComment().isBlank()) destAddress.put("comment", dest.getComment());
        fillAddressDetails(destAddress, dest);
        addCoordinatesToAddress(destAddress, destCoords);
        destPointCreate.set("address", destAddress);
        destPointCreate.set("contact", objectMapper.createObjectNode()
                .put("name", "Клиент")
                .put("phone", destPhone));

        ArrayNode itemsCreate = createBody.putArray("items");
        itemsCreate.addObject()
                .put("title", "Заказ " + order.getName())
                .put("quantity", 1)
                .put("pickup_point", 1)
                .put("dropoff_point", 2)
                .put("cost_value", "0")
                .put("cost_currency", "RUB");

        ObjectNode clientReqs = objectMapper.createObjectNode()
                .put("taxi_class", effectiveTaxiClass(taxiClass));
        clientReqs.putArray("cargo_options").add("thermobag");
        createBody.set("client_requirements", clientReqs);

        try {
            String body = objectMapper.writeValueAsString(createBody);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + CLAIMS_CREATE + "?request_id=" + requestId))
                    .header("Authorization", "Bearer " + token)
                    .header("Accept-Language", "ru")
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(20))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            log.info("Yandex claims/create response: status={}, body={}", response.statusCode(), response.body());
            if (response.statusCode() != 200) {
                log.warn("Yandex claims/create failed: {} {}", response.statusCode(), response.body());
                throw new RuntimeException("Яндекс Доставка: не удалось создать заявку. " + response.body());
            }
            JsonNode root = objectMapper.readTree(response.body());
            String claimId = root.has("id") ? root.get("id").asText() : null;
            if (claimId != null) {
                log.info("Yandex claim created: {} for order {}", claimId, order.getName());
                return claimId;
            }
            return "Заявка создана";
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Yandex claims/create error", e);
            throw new RuntimeException("Яндекс Доставка: ошибка создания заявки. " + e.getMessage());
        }
    }

    /**
     * Создаёт одну заявку на доставку по сложному маршруту: один курьер забирает в точке забора и развозит по всем выбранным заказам.
     * Порядок точек доставки = порядок заказов в списке.
     *
     * @param orders список заказов (адрес и телефон обязательны)
     * @return идентификатор созданной заявки (один на весь маршрут)
     */
    public String createComplexRouteClaim(List<OrderShortDto> orders) {
        return createComplexRouteClaim(orders, null);
    }

    public String createComplexRouteClaim(List<OrderShortDto> orders, String taxiClass) {
        if (orders == null || orders.isEmpty()) {
            throw new IllegalArgumentException("Нужен хотя бы один заказ для маршрута");
        }
        log.info("Yandex createComplexRouteClaim: {} orders", orders.size());
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("Яндекс Доставка: не задан токен (yandex.delivery.token)");
        }
        if (sourceAddress == null || sourceAddress.isBlank()) {
            throw new IllegalStateException("Яндекс Доставка: задайте адрес точки забора (yandex.delivery.source-address)");
        }
        for (OrderShortDto o : orders) {
            if (o.getAddress() == null) throw new IllegalArgumentException("У заказа " + o.getName() + " нет адреса");
            if (o.getCustomerPhone() == null || o.getCustomerPhone().isBlank()) {
                throw new IllegalArgumentException("У заказа " + o.getName() + " нет телефона клиента");
            }
        }
        String sourcePhoneForContact = sourcePhone != null && !sourcePhone.isBlank()
                ? normalizePhone(sourcePhone)
                : normalizePhone(orders.get(0).getCustomerPhone());

        // 1) Оффер: route_points с координатами (обязательны для оценки)
        double[] sourceCoords = geocode(sourceAddress);
        ObjectNode offerBody = objectMapper.createObjectNode();
        ArrayNode routePoints = offerBody.putArray("route_points");
        ObjectNode srcPt = routePoints.addObject();
        srcPt.put("id", 1).put("fullname", sourceAddress).put("country", "Россия");
        addCoordinatesToRoutePoint(srcPt, sourceCoords);
        for (int i = 0; i < orders.size(); i++) {
            OrderAddressDto dest = orders.get(i).getAddress();
            String fullname = buildFullAddress(dest);
            double[] destCoords = getDestCoords(dest, fullname);
            ObjectNode p = routePoints.addObject();
            p.put("id", i + 2).put("fullname", fullname).put("country", "Россия");
            if (dest.getCity() != null) p.put("city", dest.getCity());
            if (dest.getStreet() != null) p.put("street", dest.getStreet());
            if (dest.getHouse() != null) p.put("building", dest.getHouse());
            if (dest.getFlat() != null) p.put("sflat", dest.getFlat());
            if (dest.getFloor() != null) p.put("sfloor", dest.getFloor());
            addAddressDetailsToOfferPoint(p, dest);
            addCoordinatesToRoutePoint(p, destCoords);
        }
        ArrayNode items = offerBody.putArray("items");
        for (int i = 0; i < orders.size(); i++) {
            items.addObject()
                    .put("quantity", 1)
                    .put("pickup_point", 1)
                    .put("dropoff_point", i + 2)
                    .put("weight", 2)
                    .set("size", objectMapper.createObjectNode()
                            .put("length", 0.3)
                            .put("width", 0.3)
                            .put("height", 0.2));
        }
        ObjectNode requirements = offerBody.putObject("requirements");
        requirements.putArray("taxi_classes").add(effectiveTaxiClass(taxiClass));
        requirements.putArray("cargo_options").add("thermobag");
        requirements.put("skip_door_to_door", false);

        String offerPayload;
        try {
            String body = objectMapper.writeValueAsString(offerBody);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + OFFERS_CALCULATE))
                    .header("Authorization", "Bearer " + token)
                    .header("Accept-Language", "ru")
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(20))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                log.warn("Yandex createComplexRouteClaim offers/calculate failed: {} {}", response.statusCode(), response.body());
                throw new RuntimeException("Яндекс Доставка: не удалось рассчитать маршрут. " + response.body());
            }
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode offers = root.get("offers");
            if (offers == null || !offers.isArray() || offers.isEmpty()) {
                throw new RuntimeException("Яндекс Доставка: нет доступных вариантов по маршруту.");
            }
            offerPayload = offers.get(0).get("payload").asText();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Yandex createComplexRouteClaim offers/calculate error", e);
            throw new RuntimeException("Яндекс Доставка: ошибка расчёта маршрута. " + e.getMessage());
        }

        // 2) Создать заявку по сложному маршруту
        String requestId = UUID.randomUUID().toString();
        ObjectNode createBody = objectMapper.createObjectNode();
        createBody.put("offer_payload", offerPayload);
        createBody.put("comment", "Маршрут: " + orders.size() + " заказов");

        ArrayNode routePointsCreate = createBody.putArray("route_points");
        ObjectNode sourcePoint = routePointsCreate.addObject();
        sourcePoint.put("point_id", 1).put("visit_order", 1).put("type", "source");
        ObjectNode sourceAddressNode = objectMapper.createObjectNode().put("fullname", sourceAddress);
        if (sourceComment != null && !sourceComment.isBlank()) sourceAddressNode.put("comment", sourceComment);
        addCoordinatesToAddress(sourceAddressNode, sourceCoords);
        sourcePoint.set("address", sourceAddressNode);
        sourcePoint.set("contact", objectMapper.createObjectNode()
                .put("name", "Ресторан")
                .put("phone", sourcePhoneForContact));

        for (int i = 0; i < orders.size(); i++) {
            OrderShortDto o = orders.get(i);
            String destFullname = buildFullAddress(o.getAddress());
            double[] destCoords = getDestCoords(o.getAddress(), destFullname);
            String destPhone = normalizePhone(o.getCustomerPhone());
            ObjectNode destPoint = routePointsCreate.addObject();
            destPoint.put("point_id", i + 2)
                    .put("visit_order", i + 2)
                    .put("type", "destination")
                    .put("external_order_id", o.getName());
            ObjectNode destAddress = objectMapper.createObjectNode().put("fullname", destFullname);
            OrderAddressDto oAddr = o.getAddress();
            if (oAddr.getCity() != null) destAddress.put("city", oAddr.getCity());
            if (oAddr.getStreet() != null) destAddress.put("street", oAddr.getStreet());
            if (oAddr.getHouse() != null) destAddress.put("building", oAddr.getHouse());
            if (oAddr.getComment() != null && !oAddr.getComment().isBlank()) destAddress.put("comment", oAddr.getComment());
            fillAddressDetails(destAddress, oAddr);
            addCoordinatesToAddress(destAddress, destCoords);
            destPoint.set("address", destAddress);
            destPoint.set("contact", objectMapper.createObjectNode()
                    .put("name", "Клиент")
                    .put("phone", destPhone));
        }

        ArrayNode itemsCreate = createBody.putArray("items");
        for (int i = 0; i < orders.size(); i++) {
            itemsCreate.addObject()
                    .put("title", "Заказ " + orders.get(i).getName())
                    .put("quantity", 1)
                    .put("pickup_point", 1)
                    .put("dropoff_point", i + 2)
                    .put("cost_value", "0")
                    .put("cost_currency", "RUB");
        }
        ObjectNode clientReqs = objectMapper.createObjectNode().put("taxi_class", effectiveTaxiClass(taxiClass));
        clientReqs.putArray("cargo_options").add("thermobag");
        createBody.set("client_requirements", clientReqs);

        try {
            String body = objectMapper.writeValueAsString(createBody);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + CLAIMS_CREATE + "?request_id=" + requestId))
                    .header("Authorization", "Bearer " + token)
                    .header("Accept-Language", "ru")
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            log.info("Yandex createComplexRouteClaim response: status={}, body={}", response.statusCode(), response.body());
            if (response.statusCode() != 200) {
                log.warn("Yandex createComplexRouteClaim claims/create failed: {} {}", response.statusCode(), response.body());
                throw new RuntimeException("Яндекс Доставка: не удалось создать заявку по маршруту. " + response.body());
            }
            JsonNode root = objectMapper.readTree(response.body());
            String claimId = root.has("id") ? root.get("id").asText() : null;
            if (claimId != null) {
                log.info("Yandex complex route claim created: {} for {} orders", claimId, orders.size());
                return claimId;
            }
            return "Заявка создана";
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Yandex createComplexRouteClaim error", e);
            throw new RuntimeException("Яндекс Доставка: ошибка создания заявки по маршруту. " + e.getMessage());
        }
    }

    /**
     * Рассчитывает стоимость доставки по одному заказу (без создания заявки).
     *
     * @param order заказ с адресом доставки
     * @return цена в рублях (строка, например "250" или "312.50"), или null при ошибке
     */
    public String calculatePrice(OrderShortDto order) {
        return calculatePrice(order, null);
    }

    public String calculatePrice(OrderShortDto order, String taxiClass) {
        log.info("Yandex calculatePrice: order={}, orderId={}", order.getName(), order.getId());
        if (token == null || token.isBlank()) {
            log.warn("Yandex calculatePrice: токен не задан, возвращаем null");
            return null;
        }
        if (sourceAddress == null || sourceAddress.isBlank()) {
            log.warn("Yandex calculatePrice: не задан адрес точки забора. Задайте yandex.delivery.source-address в application.yaml или переменную YANDEX_DELIVERY_SOURCE_ADDRESS (полный адрес ресторана, напр. Санкт-Петербург, ул. Примерная, 1)");
            return null;
        }
        OrderAddressDto dest = order.getAddress();
        if (dest == null) {
            log.warn("Yandex calculatePrice: у заказа нет адреса, order={}", order.getName());
            return null;
        }
        String destFullname = buildFullAddress(dest);
        log.info("Yandex calculatePrice: source={}, dest={}", sourceAddress, destFullname);

        double[] sourceCoords = geocode(sourceAddress);
        double[] destCoords = getDestCoords(dest, destFullname);
        ObjectNode offerBody = objectMapper.createObjectNode();
        ArrayNode routePoints = offerBody.putArray("route_points");
        ObjectNode srcPt = routePoints.addObject();
        srcPt.put("id", 1).put("fullname", sourceAddress).put("country", "Россия");
        addCoordinatesToRoutePoint(srcPt, sourceCoords);
        ObjectNode destPt = routePoints.addObject();
        destPt.put("id", 2)
                .put("fullname", destFullname)
                .put("country", "Россия")
                .put("city", dest.getCity() != null ? dest.getCity() : "");
        if (dest.getStreet() != null) destPt.put("street", dest.getStreet());
        if (dest.getHouse() != null) destPt.put("building", dest.getHouse());
        if (dest.getFlat() != null) destPt.put("sflat", dest.getFlat());
        if (dest.getFloor() != null) destPt.put("sfloor", dest.getFloor());
        addAddressDetailsToOfferPoint(destPt, dest);
        addCoordinatesToRoutePoint(destPt, destCoords);

        ArrayNode items = offerBody.putArray("items");
        items.addObject()
                .put("quantity", 1)
                .put("pickup_point", 1)
                .put("dropoff_point", 2)
                .put("weight", 2)
                .set("size", objectMapper.createObjectNode()
                        .put("length", 0.3)
                        .put("width", 0.3)
                        .put("height", 0.2));

        ObjectNode requirements = offerBody.putObject("requirements");
        requirements.putArray("taxi_classes").add(effectiveTaxiClass(taxiClass));
        requirements.putArray("cargo_options").add("thermobag");
        requirements.put("skip_door_to_door", false);

        try {
            String body = objectMapper.writeValueAsString(offerBody);
            log.info("Yandex calculatePrice offers/calculate request: bodyLength={}", body.length());
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + OFFERS_CALCULATE))
                    .header("Authorization", "Bearer " + token)
                    .header("Accept-Language", "ru")
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(15))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            log.info("Yandex calculatePrice offers/calculate response: status={}, body={}", response.statusCode(), response.body());
            if (response.statusCode() != 200) {
                log.warn("Yandex calculatePrice: HTTP {} для заказа {}, body={}", response.statusCode(), order.getName(), response.body());
                return null;
            }
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode offers = root.get("offers");
            if (offers == null || !offers.isArray() || offers.isEmpty()) {
                log.warn("Yandex calculatePrice: нет офферов в ответе для заказа {}, body={}", order.getName(), response.body());
                return null;
            }
            JsonNode price = offers.get(0).get("price");
            if (price != null && price.has("total_price")) {
                String totalPrice = price.get("total_price").asText();
                log.info("Yandex calculatePrice: заказ {} стоимость {} руб", order.getName(), totalPrice);
                return totalPrice;
            }
            log.warn("Yandex calculatePrice: в первом оффере нет price.total_price, body={}", response.body());
            return null;
        } catch (Exception e) {
            log.warn("Yandex calculatePrice failed for order {}: {}", order.getName(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * Рассчитывает стоимость сложного маршрута (один курьер, несколько заказов) без создания заявки.
     *
     * @param orders   список заказов в порядке посещения (адрес обязателен)
     * @param taxiClass "express" или "courier"
     * @return общая стоимость маршрута в рублях (строка) или null при ошибке
     */
    public String calculateComplexRoutePrice(List<OrderShortDto> orders, String taxiClass) {
        if (orders == null || orders.size() < 2) {
            return null;
        }
        if (token == null || token.isBlank() || sourceAddress == null || sourceAddress.isBlank()) {
            return null;
        }
        for (OrderShortDto o : orders) {
            if (o.getAddress() == null) return null;
        }
        double[] sourceCoords = geocode(sourceAddress);
        ObjectNode offerBody = objectMapper.createObjectNode();
        ArrayNode routePoints = offerBody.putArray("route_points");
        ObjectNode srcPt = routePoints.addObject();
        srcPt.put("id", 1).put("fullname", sourceAddress).put("country", "Россия");
        addCoordinatesToRoutePoint(srcPt, sourceCoords);
        for (int i = 0; i < orders.size(); i++) {
            OrderAddressDto dest = orders.get(i).getAddress();
            String fullname = buildFullAddress(dest);
            double[] destCoords = getDestCoords(dest, fullname);
            ObjectNode p = routePoints.addObject();
            p.put("id", i + 2).put("fullname", fullname).put("country", "Россия");
            if (dest.getCity() != null) p.put("city", dest.getCity());
            if (dest.getStreet() != null) p.put("street", dest.getStreet());
            if (dest.getHouse() != null) p.put("building", dest.getHouse());
            if (dest.getFlat() != null) p.put("sflat", dest.getFlat());
            if (dest.getFloor() != null) p.put("sfloor", dest.getFloor());
            addAddressDetailsToOfferPoint(p, dest);
            addCoordinatesToRoutePoint(p, destCoords);
        }
        ArrayNode items = offerBody.putArray("items");
        for (int i = 0; i < orders.size(); i++) {
            items.addObject()
                    .put("quantity", 1)
                    .put("pickup_point", 1)
                    .put("dropoff_point", i + 2)
                    .put("weight", 2)
                    .set("size", objectMapper.createObjectNode()
                            .put("length", 0.3)
                            .put("width", 0.3)
                            .put("height", 0.2));
        }
        ObjectNode requirements = offerBody.putObject("requirements");
        requirements.putArray("taxi_classes").add(effectiveTaxiClass(taxiClass));
        requirements.putArray("cargo_options").add("thermobag");
        requirements.put("skip_door_to_door", false);
        try {
            String body = objectMapper.writeValueAsString(offerBody);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + OFFERS_CALCULATE))
                    .header("Authorization", "Bearer " + token)
                    .header("Accept-Language", "ru")
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(20))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                log.warn("Yandex calculateComplexRoutePrice failed: {} {}", response.statusCode(), response.body());
                return null;
            }
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode offers = root.get("offers");
            if (offers == null || !offers.isArray() || offers.isEmpty()) return null;
            JsonNode price = offers.get(0).get("price");
            if (price != null && price.has("total_price")) {
                String totalPrice = price.get("total_price").asText();
                log.info("Yandex calculateComplexRoutePrice: {} заказов, стоимость {} руб", orders.size(), totalPrice);
                return totalPrice;
            }
            return null;
        } catch (Exception e) {
            log.warn("Yandex calculateComplexRoutePrice failed: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Получает статус заявки в Яндекс Доставке (человекочитаемый).
     */
    public String getClaimStatus(String claimId) {
        log.info("Yandex getClaimStatus: claimId={}", claimId);
        if (token == null || token.isBlank() || claimId == null || claimId.isBlank()) {
            log.warn("Yandex getClaimStatus: токен или claimId пустой");
            return null;
        }
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + CLAIMS_INFO + "?claim_id=" + claimId))
                    .header("Authorization", "Bearer " + token)
                    .header("Accept-Language", "ru")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            log.info("Yandex getClaimStatus response: status={}, body={}", response.statusCode(), response.body());
            if (response.statusCode() != 200) {
                log.warn("Yandex getClaimStatus: HTTP {}", response.statusCode());
                return null;
            }
            JsonNode root = objectMapper.readTree(response.body());
            String status = root.has("status") ? root.get("status").asText() : null;
            String label = status != null ? mapStatusToLabel(status) : null;
            log.info("Yandex getClaimStatus: claimId={} status={} label={}", claimId, status, label);
            return label;
        } catch (Exception e) {
            log.warn("Yandex getClaimStatus failed for {}: {}", claimId, e.getMessage(), e);
            return null;
        }
    }

    private static String mapStatusToLabel(String status) {
        return switch (status) {
            case "new" -> "Новая";
            case "estimating" -> "Расчёт";
            case "estimating_failed" -> "Ошибка расчёта";
            case "ready_for_approval" -> "Готова к подтверждению";
            case "accepted" -> "Принята";
            case "performer_lookup" -> "Поиск курьера";
            case "performer_draft" -> "Курьер назначен";
            case "performer_found" -> "Курьер найден";
            case "performer_not_found" -> "Курьер не найден";
            case "pickup_arrived" -> "Курьер приехал за заказом";
            case "ready_for_pickup_confirmation" -> "Ожидание забора";
            case "pickuped" -> "Заказ забран";
            case "delivery_arrived" -> "Курьер у клиента";
            case "ready_for_delivery_confirmation" -> "Ожидание вручения";
            case "delivered" -> "Доставлен";
            case "delivered_finish" -> "Доставка завершена";
            case "returning" -> "Возврат";
            case "return_arrived" -> "Возврат у ресторана";
            case "ready_for_return_confirmation" -> "Ожидание возврата";
            case "returned" -> "Возвращён";
            case "returned_finish" -> "Возврат завершён";
            case "failed" -> "Не выполнен";
            case "cancelled", "cancelled_with_payment", "cancelled_by_taxi", "cancelled_with_items_on_hands" -> "Отменён";
            default -> status;
        };
    }

    private static String effectiveTaxiClass(String taxiClass) {
        return (taxiClass != null && !taxiClass.isBlank()) ? taxiClass : "express";
    }

    private static String buildFullAddress(OrderAddressDto a) {
        StringBuilder sb = new StringBuilder();
        if (a.getCity() != null && !a.getCity().isBlank()) sb.append(a.getCity());
        if (a.getStreet() != null && !a.getStreet().isBlank()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(a.getStreet());
        }
        if (a.getHouse() != null && !a.getHouse().isBlank()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(a.getHouse());
        }
        if (a.getFlat() != null && !a.getFlat().isBlank()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append("кв. ").append(a.getFlat());
        }
        if (a.getFloor() != null && !a.getFloor().isBlank()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append("эт. ").append(a.getFloor());
        }
        if (a.getEntrance() != null && !a.getEntrance().isBlank()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append("под. ").append(a.getEntrance());
        }
        if (a.getDoorphone() != null && !a.getDoorphone().isBlank()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append("домофон ").append(a.getDoorphone());
        }
        return sb.length() > 0 ? sb.toString() : "Адрес не указан";
    }

    /** Заполняет поля адреса (квартира, подъезд, этаж, домофон) в объекте address для API claims/create. */
    private static void fillAddressDetails(ObjectNode addressNode, OrderAddressDto a) {
        if (a == null) return;
        if (a.getFlat() != null && !a.getFlat().isBlank()) addressNode.put("sflat", a.getFlat());
        if (a.getFloor() != null && !a.getFloor().isBlank()) addressNode.put("sfloor", a.getFloor());
        if (a.getEntrance() != null && !a.getEntrance().isBlank()) addressNode.put("porch", a.getEntrance());
        if (a.getDoorphone() != null && !a.getDoorphone().isBlank()) addressNode.put("door_code", a.getDoorphone());
    }

    /** Добавляет в точку маршрута для offers/calculate поля квартира, подъезд, этаж, домофон. */
    private static void addAddressDetailsToOfferPoint(ObjectNode point, OrderAddressDto a) {
        if (a == null) return;
        if (a.getEntrance() != null && !a.getEntrance().isBlank()) point.put("porch", a.getEntrance());
        if (a.getDoorphone() != null && !a.getDoorphone().isBlank()) point.put("door_code", a.getDoorphone());
        // sflat, sfloor уже добавляются в вызывающем коде
    }

    private static String normalizePhone(String phone) {
        if (phone == null) return null;
        String p = phone.replaceAll("[^0-9+]", "");
        if (p.startsWith("8") && p.length() == 11) return "+7" + p.substring(1);
        if (p.startsWith("7") && p.length() == 11) return "+" + p;
        if (!p.startsWith("+")) return "+7" + p;
        return p;
    }
}
