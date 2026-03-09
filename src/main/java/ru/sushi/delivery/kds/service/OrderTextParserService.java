package ru.sushi.delivery.kds.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.persist.entity.ItemCombo;
import ru.sushi.delivery.kds.domain.persist.entity.product.MenuItem;
import ru.sushi.delivery.kds.dto.OrderAddressDto;
import ru.sushi.delivery.kds.dto.ParsedOrderDto;
import ru.sushi.delivery.kds.model.OrderType;
import ru.sushi.delivery.kds.model.PaymentType;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j2
@Service
@RequiredArgsConstructor
public class OrderTextParserService {

    /**
     * Парсит текст заказа и возвращает структурированные данные
     */
    public ParsedOrderDto parseOrderText(String text, List<MenuItem> allMenuItems, List<ItemCombo> allCombos) {
        ParsedOrderDto.ParsedOrderDtoBuilder builder = ParsedOrderDto.builder();
        
        // Парсим номер заказа
        String orderNumber = parseOrderNumber(text);
        builder.orderNumber(orderNumber);
        
        // Парсим комментарий (основной + комментарий после адреса, например «Строение 1» в конце строки адреса)
        String comment = mergeComments(parseComment(text), parseCommentFromAddressLine(text));
        builder.comment(comment);
        
        // Парсим время начала и готовности
        Instant kitchenStartTime = parseKitchenStartTime(text);
        builder.kitchenStartTime(kitchenStartTime);
        Instant finishTime = parseFinishTime(text);
        builder.finishTime(finishTime);
        
        // Парсим приборы
        Integer instrumentsCount = parseInstrumentsCount(text);
        builder.instrumentsCount(instrumentsCount);
        
        // Парсим тип заказа
        OrderType orderType = parseOrderType(text);
        builder.orderType(orderType);
        
        // Парсим время доставки (только для доставки)
        Instant deliveryTime = null;
        if (orderType == OrderType.DELIVERY) {
            deliveryTime = parseDeliveryTime(text);
        }
        builder.deliveryTime(deliveryTime);
        
        // Парсим город
        String city = parseCity(text);
        builder.city(city);
        
        // Парсим телефон
        String customerPhone = parseCustomerPhone(text);
        builder.customerPhone(customerPhone);
        
        // Парсим тип оплаты
        PaymentType paymentType = parsePaymentType(text);
        builder.paymentType(paymentType);
        
        // Парсим адрес (только для доставки)
        OrderAddressDto address = null;
        if (orderType == OrderType.DELIVERY) {
            address = parseAddress(text);
        }
        builder.address(address);

        // Парсим сообщение "💳Картой курьеру: {сумма} P🔸Возьмите терминал"
        String cardToCourierMessage = parseCardToCourierMessage(text);
        builder.cardToCourierMessage(cardToCourierMessage);
        
        // Парсим сеты
        List<ParsedOrderDto.ParsedCombo> combos = parseCombos(text, allCombos);
        builder.combos(combos);
        
        // Парсим отдельные позиции (исключая сеты)
        List<ParsedOrderDto.ParsedItem> items = parseItems(text, allMenuItems, combos);
        builder.items(items);
        
        // Парсим допы (extras)
        Map<String, Integer> extras = parseExtras(text, allMenuItems, items);
        
        // Парсим количество персон и добавляем палочки в допы только если instrumentsCount не указан
        // instrumentsCount уже был распарсен выше
        Integer personsCount = parsePersonsCount(text);
        
        // Проверяем, есть ли уже палочки в extras
        boolean hasSticks = extras.keySet().stream()
            .anyMatch(key -> key.toLowerCase().contains("палочки"));
        
        // Добавляем палочки автоматически только если:
        // 1. instrumentsCount не указан (null) - используем уже распарсенное значение
        // 2. Палочки еще не были добавлены вручную
        // 3. personsCount указан
        if (instrumentsCount == null && !hasSticks && personsCount != null && personsCount > 0) {
            // Ищем палочки в меню
            MenuItem sticksItem = allMenuItems.stream()
                .filter(item -> item.getName().toLowerCase().contains("палочки") || 
                              item.getName().toLowerCase().contains("приборы"))
                .findFirst()
                .orElse(null);
            
            if (sticksItem != null) {
                // Добавляем палочки в количестве, равном количеству персон
                if (extras.getOrDefault(sticksItem.getName(), 0) == 0) {
                    extras.put(sticksItem.getName(), personsCount);
                }
            } else {
                // Если не нашли палочки, добавляем с названием "Палочки"
                if (extras.getOrDefault("Палочки", 0) == 0) {
                    extras.put("Палочки", personsCount);
                }
            }
        }
        
        builder.extras(extras);
        
        return builder.build();
    }

    /**
     * Парсит сообщение вида "💳Картой курьеру: 3030 P🔸Возьмите терминал".
     * Учитывает латинскую P и кириллическую Р, переносы строк между суммой и "Возьмите".
     */
    private String parseCardToCourierMessage(String text) {
        Pattern[] patterns = {
            Pattern.compile("💳Картой курьеру:\\s*(\\d+)\\s*[PР][\\s\\S]*?Возьмите терминал", Pattern.CASE_INSENSITIVE),
            Pattern.compile("Картой курьеру:\\s*(\\d+)\\s*[PР][\\s\\S]*?Возьмите терминал", Pattern.CASE_INSENSITIVE)
        };
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                return "💳Картой курьеру: " + matcher.group(1) + " P🔸Возьмите терминал";
            }
        }
        return null;
    }

    private String parseOrderNumber(String text) {
        // Паттерны для разных форматов номеров заказов
        Pattern[] patterns = {
            Pattern.compile("№\\s*([\\d\\-A-Za-z]+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("Номер:\\s*([A-Za-z0-9]+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("Новый заказ\\s+№\\s*([A-Za-z0-9]+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("Новый заказ №\\s*([A-Za-z0-9]+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("🆕 У вас новый заказ.*?🏷 Номер:\\s*([A-Za-z0-9]+)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
            Pattern.compile("Оформлен заказ\\s+(\\d+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("Номер заказа:\\s*([A-Za-z0-9]+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("Starter ID\\s+(\\d+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("🏷 Номер:\\s*([A-Za-z0-9]+)", Pattern.CASE_INSENSITIVE)
        };
        
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
        }
        
        return "";
    }

    private String parseComment(String text) {
        // Паттерны для комментариев
        Pattern[] patterns = {
            Pattern.compile("Комментарий[\\s:]*([^\\n]+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("Комментарий:\\s*([^\\n]+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("Коммент:\\s*([^\\n]+)", Pattern.CASE_INSENSITIVE)
        };
        
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                String comment = matcher.group(1).trim();
                if (!comment.equalsIgnoreCase("Нет комментария") && 
                    !comment.equalsIgnoreCase("нет комментария") &&
                    !comment.isEmpty()) {
                    return comment;
                }
            }
        }
        
        return null;
    }

    /**
     * Извлекает комментарий, идущий в конце строки адреса в кавычках-ёлочках «...»
     * (например: "кв. 71 «Строение 1»" → "Строение 1").
     */
    private String parseCommentFromAddressLine(String text) {
        String addressLine = getAddressLineForParsing(text);
        if (addressLine == null || addressLine.isEmpty()) {
            return null;
        }
        // Ищем все вхождения «...» и склеиваем в один комментарий
        Pattern guillemetPattern = Pattern.compile("«([^»]+)»");
        Matcher m = guillemetPattern.matcher(addressLine);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(m.group(1).trim());
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    /** Объединяет два комментария в один (через пробел), пустые пропускает. */
    private static String mergeComments(String mainComment, String addressLineComment) {
        if (mainComment == null || mainComment.isBlank()) {
            return addressLineComment != null && !addressLineComment.isBlank() ? addressLineComment : null;
        }
        if (addressLineComment == null || addressLineComment.isBlank()) {
            return mainComment;
        }
        return mainComment.trim() + " " + addressLineComment.trim();
    }

    /** Возвращает строку адреса (та же логика, что и в parseAddress) для извлечения комментария и т.п. */
    private String getAddressLineForParsing(String text) {
        Pattern addressPattern = Pattern.compile(
            "(?:Адрес доставки\\s*[—-]?\\s*|Доставка:\\s*)([^\\n]+)",
            Pattern.CASE_INSENSITIVE
        );
        Matcher addressMatcher = addressPattern.matcher(text);
        if (addressMatcher.find()) {
            return addressMatcher.group(1).trim();
        }
        Pattern[] starterAddressPatterns = {
            Pattern.compile(
                "🕒К\\s+\\d{1,2}:\\d{2}\\s*[–-]\\s*\\d{1,2}:\\d{2},\\s*\\d{2}\\.\\d{2}\\.\\d{4}\\s*\\n([^\\n]+)",
                Pattern.CASE_INSENSITIVE
            ),
            Pattern.compile(
                "⏰Предзаказ\\s+к\\s+\\d{1,2}:\\d{2}\\s*[–-]\\s*\\d{1,2}:\\d{2},\\s*\\d{2}\\.\\d{2}\\.\\d{4}\\s*\\n([^\\n]+)",
                Pattern.CASE_INSENSITIVE
            )
        };
        for (Pattern pattern : starterAddressPatterns) {
            Matcher starterMatcher = pattern.matcher(text);
            if (starterMatcher.find()) {
                return starterMatcher.group(1).trim();
            }
        }
        return null;
    }

    private Instant parseKitchenStartTime(String text) {
        // Паттерны для времени начала
        Pattern[] patterns = {
            // Формат "⏰Предзаказ к 15:20 – 15:40, 03.11.2025" - вычитаем 40 минут от первого времени
            Pattern.compile("⏰Предзаказ к\\s+(\\d{1,2}:\\d{2})\\s*[–-]\\s*\\d{1,2}:\\d{2},\\s*(\\d{2})\\.(\\d{2})\\.(\\d{4})", Pattern.CASE_INSENSITIVE),
            Pattern.compile("⏰Предзаказ к\\s+(\\d{1,2}:\\d{2})", Pattern.CASE_INSENSITIVE),
            // Формат "🕒К 20:46 – 21:06, 03.11.2025" - не устанавливаем время начала
            Pattern.compile("🕒К\\s+\\d{1,2}:\\d{2}\\s*[–-]\\s*\\d{1,2}:\\d{2},\\s*\\d{2}\\.\\d{2}\\.\\d{4}", Pattern.CASE_INSENSITIVE),
            Pattern.compile("Создан в\\s+(\\d{1,2}:\\d{2})\\s+(\\d{1,2})\\s+(нояб|дек|янв|фев|мар|апр|май|июн|июл|авг|сент|окт)\\.", Pattern.CASE_INSENSITIVE),
            Pattern.compile("Принято в\\s+(\\d{1,2}:\\d{2})\\s+(\\d{1,2})\\s+(нояб|дек|янв|фев|мар|апр|май|июн|июл|авг|сент|окт)\\.", Pattern.CASE_INSENSITIVE),
            Pattern.compile("🕒К\\s+(\\d{1,2}:\\d{2})", Pattern.CASE_INSENSITIVE)
        };
        
        // Сначала проверяем формат "🕒К" с диапазоном времени - для него НЕ устанавливаем время начала
        Pattern noTimePattern = Pattern.compile(
            "🕒К\\s+\\d{1,2}:\\d{2}\\s*[–-]\\s*\\d{1,2}:\\d{2},\\s*\\d{2}\\.\\d{2}\\.\\d{4}", 
            Pattern.CASE_INSENSITIVE
        );
        if (noTimePattern.matcher(text).find()) {
            return null; // Не устанавливаем время начала для формата "🕒К"
        }
        
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                // Пропускаем паттерн "🕒К" с диапазоном, так как он уже обработан выше
                if (pattern.pattern().contains("🕒К") && pattern.pattern().contains("–")) {
                    continue;
                }
                
                try {
                    String timeStr = matcher.group(1);
                    LocalTime time = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("H:mm"));
                    LocalDate date = ZonedDateTime.now().toLocalDate(); // Используем текущую дату
                    
                    // Если есть полная дата (формат 03.11.2025)
                    if (matcher.groupCount() >= 4 && matcher.group(2) != null && matcher.group(3) != null && matcher.group(4) != null) {
                        try {
                            int day = Integer.parseInt(matcher.group(2));
                            int month = Integer.parseInt(matcher.group(3));
                            int year = Integer.parseInt(matcher.group(4));
                            date = LocalDate.of(year, month, day);
                        } catch (Exception e) {
                            // Игнорируем ошибку парсинга даты
                        }
                    }
                    // Если есть день и месяц (старый формат)
                    else if (matcher.groupCount() >= 2 && matcher.group(2) != null && matcher.group(2).matches("\\d{1,2}")) {
                        try {
                            int day = Integer.parseInt(matcher.group(2));
                            date = ZonedDateTime.now().toLocalDate().withDayOfMonth(day);
                        } catch (Exception e) {
                            // Игнорируем ошибку парсинга дня
                        }
                    }
                    
                    LocalDateTime dateTime = LocalDateTime.of(date, time);
                    
                    // Если это формат "⏰Предзаказ к", вычитаем 40 минут
                    if (pattern.pattern().contains("⏰Предзаказ")) {
                        dateTime = dateTime.minusMinutes(40);
                    }
                    
                    return dateTime.atZone(ZoneId.systemDefault()).toInstant();
                } catch (DateTimeParseException e) {
                    log.debug("Error parsing kitchen start time: {}", e.getMessage());
                }
            }
        }
        
        return null;
    }

    private Instant parseFinishTime(String text) {
        // Паттерны для времени готовности
        Pattern[] patterns = {
            Pattern.compile("Приготовить к\\s+(\\d{1,2}:\\d{2})\\s+(\\d{1,2})\\s+(нояб|дек|янв|фев|мар|апр|май|июн|июл|авг|сент|окт)\\.", Pattern.CASE_INSENSITIVE),
            Pattern.compile("🕒К\\s+(\\d{1,2}:\\d{2})[\\s–-]+(\\d{1,2}:\\d{2}),\\s+(\\d{2}\\.\\d{2}\\.\\d{4})", Pattern.CASE_INSENSITIVE),
            Pattern.compile("🕒К\\s+(\\d{1,2}:\\d{2})[\\s–-]+(\\d{1,2}:\\d{2}),\\s+(\\d{2}\\.\\d{2}\\.\\d{4})", Pattern.CASE_INSENSITIVE)
        };
        
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                try {
                    String timeStr = matcher.group(1);
                    LocalTime time = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("H:mm"));
                    LocalDate date = ZonedDateTime.now().toLocalDate();
                    
                    // Если есть день и месяц
                    if (matcher.groupCount() >= 2 && matcher.group(2) != null) {
                        try {
                            int day = Integer.parseInt(matcher.group(2));
                            date = ZonedDateTime.now().toLocalDate().withDayOfMonth(day);
                        } catch (Exception e) {
                            // Игнорируем
                        }
                    }
                    
                    // Если есть полная дата (формат 03.11.2025)
                    if (matcher.groupCount() >= 3 && matcher.group(3) != null) {
                        try {
                            date = LocalDate.parse(matcher.group(3), DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                        } catch (Exception e) {
                            // Игнорируем
                        }
                    }
                    
                    LocalDateTime dateTime = LocalDateTime.of(date, time);
                    return dateTime.atZone(ZoneId.systemDefault()).toInstant();
                } catch (DateTimeParseException e) {
                    log.debug("Error parsing finish time: {}", e.getMessage());
                }
            }
        }
        
        return null;
    }

    private Integer parseInstrumentsCount(String text) {
        // Паттерны для количества приборов
        Pattern[] patterns = {
            // Сначала проверяем формат с эмодзи (более специфичный)
            Pattern.compile("🍴\\s*Приборы:\\s*(\\d+)\\s*шт\\.?", Pattern.CASE_INSENSITIVE),
            Pattern.compile("🍴\\s*Приборы:\\s*(\\d+)\\s*шт", Pattern.CASE_INSENSITIVE),
            Pattern.compile("🍴\\s*Приборы:\\s*(\\d+)", Pattern.CASE_INSENSITIVE),
            // Затем формат без эмодзи
            Pattern.compile("Приборы:\\s*(\\d+)\\s*шт\\.?", Pattern.CASE_INSENSITIVE),
            Pattern.compile("Приборы:\\s*(\\d+)\\s*шт", Pattern.CASE_INSENSITIVE),
            Pattern.compile("Приборы:\\s*(\\d+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("Приборы\\s+(\\d+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("Палочки[\\s–-]+(\\d+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(\\d+)×\\s*Палочки", Pattern.CASE_INSENSITIVE),
            Pattern.compile("Без приборов", Pattern.CASE_INSENSITIVE),
            Pattern.compile("🍴\\s*Без приборов", Pattern.CASE_INSENSITIVE),
            Pattern.compile("·\\s*(\\d+)×\\s*Палочки", Pattern.CASE_INSENSITIVE)
        };
        
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                if (pattern.pattern().contains("Без")) {
                    return 0;
                }
                if (pattern.pattern().contains("×")) {
                    // Извлекаем количество из "N× Палочки" или "· N× Палочки"
                    try {
                        return Integer.parseInt(matcher.group(1));
                    } catch (Exception e) {
                        // Пытаемся найти число перед ×
                        String before = text.substring(Math.max(0, matcher.start() - 10), matcher.start());
                        Pattern countPattern = Pattern.compile("(\\d+)\\s*×");
                        Matcher countMatcher = countPattern.matcher(before);
                        if (countMatcher.find()) {
                            return Integer.parseInt(countMatcher.group(1));
                        }
                        return 1;
                    }
                }
                try {
                    String group = matcher.group(1);
                    if (group != null && !group.isEmpty()) {
                        return Integer.parseInt(group);
                    }
                } catch (Exception e) {
                    log.debug("Error parsing instruments count: {}", e.getMessage());
                }
            }
        }
        
        return null;
    }

    private List<ParsedOrderDto.ParsedCombo> parseCombos(String text, List<ItemCombo> allCombos) {
        List<ParsedOrderDto.ParsedCombo> combos = new ArrayList<>();
        
        // Ищем блоки с сетами - используем findAll для поиска всех блоков
        Pattern comboPattern = Pattern.compile(
            "(?:Сеты|Сеты за \\d+|📝 Состав:|Состав:)\\s*(.*?)(?=(?:Сеты|Сеты за \\d+|Дополнительно|Итого|Стоимость заказа|Телефон|Доставка|Оплата|Количество персон|Комментарий|🍴|Примите|\\n\\n|$))",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );
        Matcher comboBlockMatcher = comboPattern.matcher(text);
        
        while (comboBlockMatcher.find()) {
            String comboBlock = comboBlockMatcher.group(1);
            
            // Паттерн для строк вида "1 х Сет Название" или "1× Сет Название" или "• 1 x Сет Название" или "· 1× Сет Название"
            // или формата "Название [цена] | цена x количество = итого"
            // Упрощенный паттерн - захватывает все до конца строки
            Pattern itemPattern = Pattern.compile(
                "[•·]?\\s*(\\d+)\\s*[х×x]\\s*([^\\n\\r]+)",
                Pattern.CASE_INSENSITIVE
            );
            Matcher itemMatcher = itemPattern.matcher(comboBlock);
            
            while (itemMatcher.find()) {
                int quantity = Integer.parseInt(itemMatcher.group(1));
                String name = itemMatcher.group(2).trim();
                
                // Проверяем, что это сет (начинается с "Сет" или содержит слово "Сет")
                if (!name.toLowerCase().contains("сет")) {
                    continue;
                }
                
                // Убираем лишнее (вес, г, цену и т.д.)
                name = name.replaceAll("\\s+\\d+\\s*г", "").trim();
                name = name.replaceAll("\\s+\\d+\\s*кг", "").trim();
                name = name.replaceAll("\\s*–\\s*\\d+\\s*P", "").trim();
                name = name.replaceAll("\\s*\\d+\\s*₽", "").trim();
                name = name.replaceAll("\\s*\\[.*?\\]", "").trim(); // Убираем [1510 руб]
                name = name.replaceAll("\\s*\\|.*$", "").trim(); // Убираем всё после |
                name = name.replaceAll("\\s+", " ").trim(); // Нормализуем пробелы
                
                // Ищем соответствующий ItemCombo
                ItemCombo foundCombo = findComboByName(allCombos, name);
                
                combos.add(ParsedOrderDto.ParsedCombo.builder()
                    .name(name)
                    .quantity(quantity)
                    .combo(foundCombo)
                    .build());
            }
            
            // Также ищем сеты в формате "Сет Название [цена] | цена x количество = итого"
            Pattern newFormatComboPattern = Pattern.compile(
                "(Сет\\s+[^\\n]+?)\\s*\\[.*?\\]\\s*\\|\\s*\\d+\\s*[х×x]\\s*(\\d+)\\s*=",
                Pattern.CASE_INSENSITIVE
            );
            Matcher newFormatComboMatcher = newFormatComboPattern.matcher(comboBlock);
            
            while (newFormatComboMatcher.find()) {
                String rawName = newFormatComboMatcher.group(1).trim();
                int quantity = Integer.parseInt(newFormatComboMatcher.group(2));
                
                // Убираем лишнее
                String name = rawName.replaceAll("\\s*\\[.*?\\]", "").trim();
                name = name.replaceAll("\\s*\\|.*$", "").trim();
                
                // Проверяем, не добавлен ли уже этот сет
                String finalName = name;
                boolean alreadyAdded = combos.stream()
                    .anyMatch(c -> c.getName().toLowerCase().equals(finalName.toLowerCase()));
                
                if (!alreadyAdded) {
            ItemCombo foundCombo = findComboByName(allCombos, name);
                    combos.add(ParsedOrderDto.ParsedCombo.builder()
                        .name(name)
                        .quantity(quantity)
                        .combo(foundCombo)
                        .build());
                }
            }
        }
        
        // Также ищем сеты в формате Starter (· 1× Сет Все включено – 2350 P) или (• 1 x Сет Атлантика)
        Pattern starterComboPattern = Pattern.compile(
            "[•·]\\s*(\\d+)\\s*[х×x]\\s*Сет\\s+([^\\n]+?)(?:\\s*–\\s*\\d+\\s*P|\\s*\\n|$)",
            Pattern.CASE_INSENSITIVE
        );
        Matcher starterComboMatcher = starterComboPattern.matcher(text);
        
        while (starterComboMatcher.find()) {
            int quantity = Integer.parseInt(starterComboMatcher.group(1));
            String name = "Сет " + starterComboMatcher.group(2).trim();
            
            // Убираем цену
            name = name.replaceAll("\\s*–\\s*\\d+\\s*P", "").trim();
            name = name.replaceAll("\\s+", " ").trim(); // Нормализуем пробелы
            
            // Проверяем, не добавлен ли уже этот сет
            String finalName = name;
            boolean alreadyAdded = combos.stream()
                .anyMatch(c -> c.getName().toLowerCase().equals(finalName.toLowerCase()));
            
            if (!alreadyAdded) {
                ItemCombo foundCombo = findComboByName(allCombos, name);
                // Добавляем сет в список независимо от того, найден он или нет
                combos.add(ParsedOrderDto.ParsedCombo.builder()
                    .name(name)
                    .quantity(quantity)
                    .combo(foundCombo) // Может быть null если не найдено
                    .build());
            }
        }
        
        return combos;
    }

    private List<ParsedOrderDto.ParsedItem> parseItems(String text, List<MenuItem> allMenuItems, List<ParsedOrderDto.ParsedCombo> parsedCombos) {
        List<ParsedOrderDto.ParsedItem> items = new ArrayList<>();
        Map<String, ParsedOrderDto.ParsedItem> processedPositions = new HashMap<>(); // Для отслеживания уже обработанных позиций и суммирования количества
        
        // Собираем названия всех сетов, чтобы их исключить
        List<String> comboNames = new ArrayList<>();
        for (ParsedOrderDto.ParsedCombo combo : parsedCombos) {
            comboNames.add(combo.getName().toLowerCase());
        }
        
        // Ищем отдельные позиции (не в блоке "Сеты" и не в блоке "Дополнительно")
        // Это может быть роллы, напитки и т.д.
        
        // Формат "· 1× Нигири с угрем – 560 P" (Starter) или "· 3× Филадельфия – 1830 P"
        Pattern starterItemPattern = Pattern.compile(
            "·\\s*(\\d+)×\\s*([^\\n]+?)(?:\\s*–\\s*[^\\n]+?|$)",
            Pattern.CASE_INSENSITIVE
        );
        Matcher starterItemMatcher = starterItemPattern.matcher(text);
        
        while (starterItemMatcher.find()) {
            int quantity = Integer.parseInt(starterItemMatcher.group(1));
            String name = starterItemMatcher.group(2).trim();
            
            // Пропускаем сеты (они уже обработаны)
            if (name.toLowerCase().contains("сет")) {
                continue;
            }
            
            // Пропускаем допы (они обрабатываются отдельно)
            if (name.toLowerCase().contains("васаби") || 
                name.toLowerCase().contains("имбирь") ||
                name.toLowerCase().contains("соевый соус") ||
                name.toLowerCase().contains("палочки") ||
                name.toLowerCase().contains("приборы")) {
                continue;
            }
            
            // Убираем цену и лишнее
            name = name.replaceAll("\\s*–\\s*[^\\n]+?P", "").trim();
            name = name.replaceAll("\\s*–\\s*Подарок.*", "").trim();
            name = name.replaceAll("\\s*–\\s*Бесплатно", "").trim();
            name = name.replaceAll("\\*$", "").trim(); // Убираем звездочку в конце
            String normalizedName = normalizeName(name);
            
            // Проверяем, не обрабатывали ли мы уже эту позицию
            if (processedPositions.containsKey(normalizedName)) {
                // Если позиция уже найдена, суммируем количество
                ParsedOrderDto.ParsedItem existingItem = processedPositions.get(normalizedName);
                existingItem.setQuantity(existingItem.getQuantity() + quantity);
                continue;
            }
            
            // Ищем соответствующий MenuItem
            MenuItem foundItem = findMenuItemByName(allMenuItems, name);
            
            // Добавляем позицию в список независимо от того, найдена она или нет
            ParsedOrderDto.ParsedItem parsedItem = ParsedOrderDto.ParsedItem.builder()
                .name(name)
                .quantity(quantity)
                .menuItem(foundItem) // Может быть null если не найдено
                .build();
            items.add(parsedItem);
            processedPositions.put(normalizedName, parsedItem);
        }
        
        // Формат "• 1 x Сет Жар-птица" (Telegram) - только если не обработали в Starter формате
        Pattern telegramItemPattern = Pattern.compile(
            "[•·]\\s*(\\d+)\\s*[х×x]\\s*([^\\n]+?)(?:\\s*–\\s*\\d+\\s*P|\\s*\\d+\\s*₽|$)",
            Pattern.CASE_INSENSITIVE
        );
        Matcher telegramItemMatcher = telegramItemPattern.matcher(text);
        
        while (telegramItemMatcher.find()) {
            int quantity = Integer.parseInt(telegramItemMatcher.group(1));
            String name = telegramItemMatcher.group(2).trim();
            
            // Пропускаем сеты (они уже обработаны)
            String finalName = name;
            if (comboNames.stream().anyMatch(comboName -> finalName.toLowerCase().contains(comboName)) ||
                finalName.toLowerCase().contains("сет")) {
                continue;
            }
            
            // Пропускаем допы
            if (name.toLowerCase().contains("васаби") || 
                name.toLowerCase().contains("имбирь") ||
                name.toLowerCase().contains("соевый соус") ||
                name.toLowerCase().contains("палочки") ||
                name.toLowerCase().contains("приборы")) {
                continue;
            }
            
            // Убираем лишнее
            name = name.replaceAll("\\s*–\\s*\\d+\\s*P", "").trim();
            name = name.replaceAll("\\s*\\d+\\s*₽", "").trim();
            String normalizedName = normalizeName(name);
            
            // Проверяем, не обрабатывали ли мы уже эту позицию другим паттерном
            // Если позиция уже найдена, пропускаем (не суммируем, чтобы избежать двойного подсчета)
            if (processedPositions.containsKey(normalizedName)) {
                continue;
            }
            
            // Ищем соответствующий MenuItem
            MenuItem foundItem = findMenuItemByName(allMenuItems, name);
            
            // Добавляем позицию в список независимо от того, найдена она или нет
            ParsedOrderDto.ParsedItem parsedItem = ParsedOrderDto.ParsedItem.builder()
                .name(name)
                .quantity(quantity)
                .menuItem(foundItem) // Может быть null если не найдено
                .build();
            items.add(parsedItem);
            processedPositions.put(normalizedName, parsedItem);
        }
        
        // Формат "Название [цена руб/балл] | цена x количество = итого" (новый формат)
        Pattern newFormatPattern = Pattern.compile(
            "([^\\n]+?)\\s*\\[.*?\\]\\s*\\|\\s*\\d+\\s*[х×x]\\s*(\\d+)\\s*=",
            Pattern.CASE_INSENSITIVE
        );
        Matcher newFormatMatcher = newFormatPattern.matcher(text);
        
        while (newFormatMatcher.find()) {
            String name = newFormatMatcher.group(1).trim();
            int quantity = Integer.parseInt(newFormatMatcher.group(2));
            
            // Пропускаем сеты (они уже обработаны)
            if (name.toLowerCase().contains("сет")) {
                continue;
            }
            
            // Пропускаем допы (они обрабатываются отдельно в parseExtras)
            if (name.toLowerCase().contains("васаби") || 
                name.toLowerCase().contains("имбирь") ||
                name.toLowerCase().contains("соевый соус") ||
                name.toLowerCase().contains("палочки") ||
                name.toLowerCase().contains("приборы")) {
                continue;
            }
            
            // Убираем лишнее
            name = name.replaceAll("\\[.*?\\]", "").trim();
            String normalizedName = normalizeName(name);
            
            // Проверяем, не обрабатывали ли мы уже эту позицию другим паттерном
            // Если позиция уже найдена, пропускаем (не суммируем, чтобы избежать двойного подсчета)
            if (processedPositions.containsKey(normalizedName)) {
                continue;
            }
            
            // Ищем соответствующий MenuItem
            MenuItem foundItem = findMenuItemByName(allMenuItems, name);
            
            // Добавляем позицию в список независимо от того, найдена она или нет
            ParsedOrderDto.ParsedItem parsedItem = ParsedOrderDto.ParsedItem.builder()
                .name(name)
                .quantity(quantity)
                .menuItem(foundItem) // Может быть null если не найдено
                .build();
            items.add(parsedItem);
            processedPositions.put(normalizedName, parsedItem);
        }
        
        // Старый формат (обычный)
        Pattern itemPattern = Pattern.compile(
            "(\\d+)\\s*[х×]\\s*([^\\n]+?)(?:\\s+\\d+\\s+\\d+\\s*₽|\\s*\\d+\\s*₽|\\s+\\d+\\s*руб|\\s+\\d+\\s*балл|$)",
            Pattern.CASE_INSENSITIVE
        );
        Matcher itemMatcher = itemPattern.matcher(text);
        
        while (itemMatcher.find()) {
            int quantity = Integer.parseInt(itemMatcher.group(1));
            String name = itemMatcher.group(2).trim();
            
            // Пропускаем сеты (они уже обработаны)
            String finalName = name;
            if (comboNames.stream().anyMatch(comboName -> finalName.toLowerCase().contains(comboName))) {
                continue;
            }
            
            // Пропускаем допы (они обрабатываются отдельно)
            if (name.toLowerCase().contains("васаби") || 
                name.toLowerCase().contains("имбирь") ||
                name.toLowerCase().contains("соевый соус") ||
                name.toLowerCase().contains("палочки") ||
                name.toLowerCase().contains("приборы")) {
                continue;
            }
            
            // Убираем лишнее
            name = name.replaceAll("\\s+\\d+\\s*г", "").trim();
            name = name.replaceAll("\\[.*?\\]", "").trim(); // Убираем [380 балл]
            String normalizedName = normalizeName(name);
            
            // Проверяем, не обрабатывали ли мы уже эту позицию другим паттерном
            // Если позиция уже найдена, пропускаем (не суммируем, чтобы избежать двойного подсчета)
            if (processedPositions.containsKey(normalizedName)) {
                continue;
            }
            
            // Ищем соответствующий MenuItem
            MenuItem foundItem = findMenuItemByName(allMenuItems, name);
            
            // Добавляем позицию в список независимо от того, найдена она или нет
            ParsedOrderDto.ParsedItem parsedItem = ParsedOrderDto.ParsedItem.builder()
                .name(name)
                .quantity(quantity)
                .menuItem(foundItem) // Может быть null если не найдено
                .build();
            items.add(parsedItem);
            processedPositions.put(normalizedName, parsedItem);
        }
        
        // Универсальный паттерн для поиска всех позиций вида "1 x Название" или "1× Название" или "• 1 x Название"
        // ВАЖНО: этот паттерн пропускает позиции, которые уже были найдены специфичными паттернами выше
        // Также поддерживаем формат с переносом строки: "1 x Название г\nцена ₽"
        // Ищем по всему тексту, исключая только уже обработанные позиции
        // НЕ ищем паттерны вида "1510 x 1 = 1510 руб" (цена x количество = итого)
        Pattern universalItemPattern = Pattern.compile(
            "(?:^|\\n|\\r|[•·]|\\s)(\\d+)\\s*[х×x]\\s*([^\\n]+?)(?:\\s+\\d+\\s*г)?(?:\\s*\\n\\s*\\d+\\s*₽|\\s*\\n\\s*\\d+\\s*[×х]\\s*\\d+\\s*₽|\\s+\\d+\\s*₽|\\s*\\d+\\s*руб|\\s*–\\s*[^\\n]*|\\s*$)(?!\\s*[=×х]\\s*\\d)", 
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE
        );
        Matcher universalItemMatcher = universalItemPattern.matcher(text);
        
        while (universalItemMatcher.find()) {
            int quantity = Integer.parseInt(universalItemMatcher.group(1));
            String name = universalItemMatcher.group(2).trim();
            
            // Пропускаем пустые названия
            if (name.isEmpty()) {
                continue;
            }
            
            // Пропускаем если это часть формата "цена x количество = итого" или "| цена x количество ="
            // Проверяем контекст до и после совпадения
            int matchStart = universalItemMatcher.start();
            int matchEnd = universalItemMatcher.end();
            
            // Проверяем контекст до совпадения - если есть "|" или "]", это формат "Название [цена] | цена x количество ="
            if (matchStart > 0) {
                String beforeMatch = text.substring(Math.max(0, matchStart - 20), matchStart);
                if (beforeMatch.contains("|") || beforeMatch.contains("]")) {
                    continue; // Это формат "Название [цена] | цена x количество =", пропускаем (уже обработано)
                }
            }
            
            // Проверяем контекст после совпадения - если есть "=" или "x" с цифрой, это формат "цена x количество = итого"
            if (matchEnd < text.length()) {
                String afterMatch = text.substring(matchEnd, Math.min(matchEnd + 50, text.length()));
                if (afterMatch.matches("\\s*[=×х]\\s*\\d+.*")) {
                    continue; // Это формат "цена x количество = итого", пропускаем
                }
            }
            
            // Пропускаем сеты (они уже обработаны)
            if (name.toLowerCase().contains("сет")) {
                continue;
            }
            
            // Пропускаем только стандартные допы из блока "Дополнительно" (они обрабатываются отдельно)
            // Но пропускаем только если это явно в блоке "Дополнительно"
            // Соусы типа "Спайси соус" должны парситься как позиции
            boolean isStandardExtra = name.toLowerCase().contains("васаби") || 
                name.toLowerCase().contains("имбирь") ||
                (name.toLowerCase().contains("соевый соус") && !name.toLowerCase().contains("спайси")) ||
                name.toLowerCase().contains("палочки") ||
                name.toLowerCase().contains("приборы");

            // Проверяем, находится ли позиция в блоке "Дополнительно"
            // String textBeforeMatch = text.substring(0, matchStart);
            // boolean isInExtrasBlock = textBeforeMatch.toLowerCase().contains("дополнительно") &&
            //     !textBeforeMatch.substring(textBeforeMatch.toLowerCase().lastIndexOf("дополнительно")).toLowerCase().contains("итого");
            
            // // Если это стандартный доп и в блоке "Дополнительно" - пропускаем
            // if (isStandardExtra && isInExtrasBlock) {

            // Стандартные допы не считаем позициями, вне зависимости от блока
            if (isStandardExtra) {
                continue;
            }
            
            // Убираем цену, вес и лишнее
            name = name.replaceAll("\\s+\\d+\\s*г", "").trim();
            name = name.replaceAll("\\s+\\d+\\s*₽", "").trim();
            name = name.replaceAll("\\s*–\\s*Подарок.*", "").trim();
            name = name.replaceAll("\\s*–\\s*Бесплатно", "").trim();
            name = name.replaceAll("\\s*–\\s*\\d+\\s*P", "").trim();
            name = name.replaceAll("\\s*–\\s*\\d+\\s*руб", "").trim();
            name = name.replaceAll("\\s*–\\s*\\d+\\s*балл", "").trim();
            name = name.replaceAll("\\s*–\\s*[^\\n]*", "").trim();
            name = name.replaceAll("\\*$", "").trim(); // Убираем звездочку в конце
            name = name.replaceAll("\\[.*?\\]", "").trim();
            
            // Убираем префиксы типа "Выберите соусы" если они есть
            name = name.replaceAll("^Выберите\\s+[^\\n]+\\s*", "").trim();
            
            // Если название пустое после очистки - пропускаем
            if (name.isEmpty()) {
                continue;
            }
            
            String normalizedName = normalizeName(name);
            
            // Проверяем, не обрабатывали ли мы уже эту позицию специфичными паттернами
            // Универсальный паттерн НЕ суммирует количество, а просто пропускает уже найденные позиции
            if (processedPositions.containsKey(normalizedName)) {
                // Позиция уже найдена специфичным паттерном, пропускаем
                continue;
            }
            
            // Ищем соответствующий MenuItem
            MenuItem foundItem = findMenuItemByName(allMenuItems, name);
            
            // Добавляем позицию в список независимо от того, найдена она или нет
            ParsedOrderDto.ParsedItem parsedItem = ParsedOrderDto.ParsedItem.builder()
                .name(name)
                .quantity(quantity)
                .menuItem(foundItem) // Может быть null если не найдено
                .build();
            items.add(parsedItem);
            processedPositions.put(normalizedName, parsedItem);
        }
        
        return items;
    }

    private Map<String, Integer> parseExtras(String text, List<MenuItem> allMenuItems, List<ParsedOrderDto.ParsedItem> parsedItems) {
        Map<String, Integer> extras = new HashMap<>();
        
        // Собираем нормализованные названия всех найденных позиций для исключения
        Set<String> foundItemNames = new HashSet<>();
        for (ParsedOrderDto.ParsedItem item : parsedItems) {
            foundItemNames.add(normalizeName(item.getName()));
        }
        
        // Ищем все блоки "Дополнительно" - может быть несколько блоков
        Pattern extrasPattern = Pattern.compile(
            "Дополнительно\\s+(.*?)(?=(?:Дополнительно|Итого|Стоимость заказа|Телефон|Доставка|Оплата|Количество персон|Комментарий|\\n\\n|$))",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );
        Matcher extrasBlockMatcher = extrasPattern.matcher(text);
        
        while (extrasBlockMatcher.find()) {
            String extrasBlock = extrasBlockMatcher.group(1);
            
            // Паттерн для строк вида "2 х Васаби 15 г" или "2× Васаби" или "Название [цена] | цена x количество = итого"
            // Также поддерживаем формат где цена на отдельной строке: "2 х Васаби 15 г\n2 × 30 ₽" или "1 x Унаги соус 40 г\n60 ₽"
            // Используем DOTALL чтобы паттерн захватывал переносы строк
            Pattern itemPattern = Pattern.compile(
                "(\\d+)\\s*[х×]\\s*([^\\n]+?)(?:\\s+\\d+\\s*г)?(?:\\s*\\n\\s*\\d+\\s*₽|\\s*\\n\\s*\\d+\\s*[×х]\\s*\\d+\\s*₽|\\s+\\d+\\s*₽|\\s*\\d+\\s*руб|\\s*Бесплатно|\\s*\\[.*?\\]|\\s*$)",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE
            );
            Matcher itemMatcher = itemPattern.matcher(extrasBlock);
            
            while (itemMatcher.find()) {
                int quantity = Integer.parseInt(itemMatcher.group(1));
                String name = itemMatcher.group(2).trim();
                
                // Убираем вес и цену
                name = name.replaceAll("\\s+\\d+\\s*г", "").trim();
                name = name.replaceAll("\\s+\\d+\\s*₽", "").trim();
                name = name.replaceAll("\\s+Бесплатно", "").trim();
                name = name.replaceAll("\\s*\\[.*?\\]", "").trim(); // Убираем [30 руб]
                name = name.replaceAll("\\s*\\|.*$", "").trim(); // Убираем всё после |
                
                // Пропускаем пустые названия или строки которые являются только ценой
                if (name.isEmpty() || name.matches("^\\d+\\s*[×х]\\s*\\d+\\s*₽$") || name.matches("^\\d+\\s*₽$")) {
                    continue;
                }
                
                // Пропускаем позиции которые не являются допами (соусы могут быть допами)
                // Но если это в блоке "Дополнительно", то это точно доп
                if (!name.toLowerCase().contains("соус") && 
                    !name.toLowerCase().contains("васаби") && 
                    !name.toLowerCase().contains("имбирь") &&
                    !name.toLowerCase().contains("соевый соус") &&
                    !name.toLowerCase().contains("палочки") &&
                    !name.toLowerCase().contains("приборы")) {
                    // Это может быть позиция, а не доп - пропускаем
                    continue;
                }
                
                // Проверяем, не является ли эта позиция уже найденной позицией
                String normalizedName = normalizeName(name);
                if (foundItemNames.contains(normalizedName)) {
                    continue; // Пропускаем, если позиция уже есть в списке найденных позиций
                }
                
                extras.put(name, extras.getOrDefault(name, 0) + quantity);
            }
        }
        
        // Также ищем допы в формате "Название [цена] | цена x количество = итого" из блока "Состав:"
        Pattern compositionExtrasPattern = Pattern.compile(
            "(?:Состав:|📝 Состав:)\\s+(.*?)(?=(?:Стоимость заказа|Итого|Телефон|Доставка|Оплата|Количество персон|Комментарий|$))",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );
        Matcher compositionExtrasMatcher = compositionExtrasPattern.matcher(text);
        
        if (compositionExtrasMatcher.find()) {
            String compositionBlock = compositionExtrasMatcher.group(1);
            
            // Паттерн для формата "Название [цена] | цена x количество = итого"
            Pattern compositionItemPattern = Pattern.compile(
                "([^\\n]+?)\\s*\\[.*?\\]\\s*\\|\\s*\\d+\\s*[х×x]\\s*(\\d+)\\s*=",
                Pattern.CASE_INSENSITIVE
            );
            Matcher compositionItemMatcher = compositionItemPattern.matcher(compositionBlock);
            
            while (compositionItemMatcher.find()) {
                String name = compositionItemMatcher.group(1).trim();
                int quantity = Integer.parseInt(compositionItemMatcher.group(2));
                
                // Проверяем, что это доп (не сет и не основное блюдо)
                if (name.toLowerCase().contains("сет")) {
                    continue;
                }
                
                // Проверяем, что это именно допы
                if (name.toLowerCase().contains("васаби") || 
                    name.toLowerCase().contains("имбирь") ||
                    name.toLowerCase().contains("соевый соус") ||
                    name.toLowerCase().contains("палочки") ||
                    name.toLowerCase().contains("приборы")) {
                    
                    // Убираем лишнее
                    name = name.replaceAll("\\s*\\[.*?\\]", "").trim();
                    name = name.replaceAll("\\s*\\|.*$", "").trim();
                    
                    // Проверяем, не является ли эта позиция уже найденной позицией
                    String normalizedName = normalizeName(name);
                    if (foundItemNames.contains(normalizedName)) {
                        continue; // Пропускаем, если позиция уже есть в списке найденных позиций
                    }
                    
                    extras.put(name, extras.getOrDefault(name, 0) + quantity);
                }
            }
        }
        
        // Также ищем отдельные строки с допами (для формата Starter)
        Pattern starterExtrasPattern = Pattern.compile(
            "·\\s*(\\d+)×\\s*([^\\n]+?)(?:\\s*–\\s*(?:Бесплатно|\\d+\\s*P)|$)",
            Pattern.CASE_INSENSITIVE
        );
        Matcher starterMatcher = starterExtrasPattern.matcher(text);
        
        while (starterMatcher.find()) {
            int quantity = Integer.parseInt(starterMatcher.group(1));
            String name = starterMatcher.group(2).trim();
            
            // Убираем цену
            name = name.replaceAll("\\s*–\\s*\\d+\\s*P", "").trim();
            name = name.replaceAll("\\s*–\\s*Бесплатно", "").trim();
            
            // Пропускаем основные позиции (они уже обработаны)
            if (!name.toLowerCase().contains("васаби") && 
                !name.toLowerCase().contains("имбирь") &&
                !name.toLowerCase().contains("соевый соус") &&
                !name.toLowerCase().contains("палочки") &&
                !name.toLowerCase().contains("приборы")) {
                continue;
            }
            
            // Проверяем, не является ли эта позиция уже найденной позицией
            String normalizedName = normalizeName(name);
            if (foundItemNames.contains(normalizedName)) {
                continue; // Пропускаем, если позиция уже есть в списке найденных позиций
            }
            
            extras.put(name, extras.getOrDefault(name, 0) + quantity);
        }
        
        return extras;
    }
    
    private Integer parsePersonsCount(String text) {
        // Паттерны для количества персон
        Pattern[] patterns = {
            Pattern.compile("Количество персон:\\s*(\\d+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("Количество персон\\s+(\\d+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("Персон:\\s*(\\d+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("Персон\\s+(\\d+)", Pattern.CASE_INSENSITIVE)
        };
        
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                try {
                    return Integer.parseInt(matcher.group(1));
                } catch (Exception e) {
                    log.debug("Error parsing persons count: {}", e.getMessage());
                }
            }
        }
        
        return null;
    }

    private ItemCombo findComboByName(List<ItemCombo> allCombos, String name) {
        String normalizedName = normalizeName(name);

        for (ItemCombo combo : allCombos) {
            String comboName = normalizeName(combo.getName());
            if (comboName.equals(normalizedName) ||
                comboName.contains(normalizedName) ||
                normalizedName.contains(comboName)) {
                return combo;
            }
        }

        return null;
    }

    private MenuItem findMenuItemByName(List<MenuItem> allMenuItems, String name) {
        String normalizedName = normalizeName(name);

        for (MenuItem item : allMenuItems) {
            String itemName = normalizeName(item.getName());
            if (itemName.equals(normalizedName)) {
                return item;
            }
        }

        return null;
    }

    private String normalizeName(String name) {
        if (name == null) return "";
        return name.toLowerCase()
            .replaceAll("\\s+", " ")
            .trim();
    }

    private Instant parseDeliveryTime(String text) {
        // Паттерны для времени доставки:
        // "🕒К 16:30 – 16:50, 06.02.2026"
        // "⏰Предзаказ к 16:50 – 17:10, 07.02.2026"
        // "Доставить  к 22:40"
        // "Доставить — к 22:40"
        
        Pattern[] patterns = {
            Pattern.compile("🕒К\\s+(\\d{1,2}):(\\d{2})\\s*[–-]\\s*\\d{1,2}:\\d{2},\\s*(\\d{2})\\.(\\d{2})\\.(\\d{4})", Pattern.CASE_INSENSITIVE),
            Pattern.compile("⏰Предзаказ\\s+к\\s+(\\d{1,2}):(\\d{2})\\s*[–-]\\s*\\d{1,2}:\\d{2},\\s*(\\d{2})\\.(\\d{2})\\.(\\d{4})", Pattern.CASE_INSENSITIVE),
            Pattern.compile("Доставить\\s*[—-]?\\s*к\\s+(\\d{1,2}):(\\d{2})", Pattern.CASE_INSENSITIVE),
            Pattern.compile("К\\s+(\\d{1,2}):(\\d{2})", Pattern.CASE_INSENSITIVE)
        };
        
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                try {
                    int hour = Integer.parseInt(matcher.group(1));
                    int minute = Integer.parseInt(matcher.group(2));
                    
                    LocalDate date;
                    if (matcher.groupCount() >= 5) {
                        // Есть дата
                        int day = Integer.parseInt(matcher.group(3));
                        int month = Integer.parseInt(matcher.group(4));
                        int year = Integer.parseInt(matcher.group(5));
                        date = LocalDate.of(year, month, day);
                    } else {
                        // Нет даты - используем сегодня
                        date = ZonedDateTime.now().toLocalDate();
                    }
                    
                    LocalDateTime dateTime = LocalDateTime.of(date, LocalTime.of(hour, minute));
                    return dateTime.atZone(ZoneId.systemDefault()).toInstant();
                } catch (Exception e) {
                    log.warn("Не удалось распарсить время доставки: {}", matcher.group(), e);
                }
            }
        }
        
        // Если время не найдено, возвращаем +50 минут от текущего времени
        return ZonedDateTime.now().toInstant().plusSeconds(50 * 60);
    }

    private String parseCity(String text) {
        // Паттерны для определения города
        Pattern ukhtaPattern = Pattern.compile("Ухта", Pattern.CASE_INSENSITIVE);
        Pattern parnasPattern = Pattern.compile("Парнас|Парголово", Pattern.CASE_INSENSITIVE);
        
        // Проверяем Ухту
        if (ukhtaPattern.matcher(text).find()) {
            return "Ухта";
        }
        
        // Проверяем Парнас/Парголово
        if (parnasPattern.matcher(text).find()) {
            return "Парнас";
        }
        
        // Если город не найден, возвращаем null
        return null;
    }

    private boolean isKnownCity(String s) {
        String lower = s.toLowerCase();
        return lower.equals("ухта") || lower.equals("парнас") || lower.equals("парголово");
    }

    /** Проверяет, что часть адреса — город или "посёлок + город" (например "посёлок Парголово"). */
    private boolean isCityPart(String part) {
        String trimmed = part.trim();
        if (isKnownCity(trimmed)) return true;
        if (trimmed.toLowerCase().startsWith("посёлок")) {
            String cityName = trimmed.replaceFirst("^посёлок\\s+", "").trim();
            return isKnownCity(cityName);
        }
        return false;
    }

    /** Извлекает название города из части адреса; "Парголово" нормализует в "Парнас" для единообразия с parseCity(). */
    private String extractCityFromPart(String part) {
        String city = part.toLowerCase().startsWith("посёлок")
                ? part.replaceFirst("^посёлок\\s+", "").trim()
                : part.trim();
        if (city.equalsIgnoreCase("Парголово")) return "Парнас";
        return city;
    }

    private boolean looksLikeHouseNumber(String s) {
        String firstWord = s.split("\\s+")[0];
        return firstWord.matches("\\d+[a-zA-Zа-яА-ЯкК]*");
    }

    private OrderType parseOrderType(String text) {
        // Паттерны для определения типа заказа
        Pattern[] deliveryPatterns = {
            Pattern.compile("🚙\\s*Доставка", Pattern.CASE_INSENSITIVE),
            Pattern.compile("Доставка\\s*·", Pattern.CASE_INSENSITIVE),
            Pattern.compile("Доставить", Pattern.CASE_INSENSITIVE),
            Pattern.compile("Адрес доставки\\s*—", Pattern.CASE_INSENSITIVE),
            Pattern.compile("Курьер\\s*—\\s*\\.+", Pattern.CASE_INSENSITIVE),
            Pattern.compile("Статус курьера\\s+Ищем", Pattern.CASE_INSENSITIVE)
        };
        
        Pattern[] pickupPatterns = {
            Pattern.compile("🥡\\s*С собой", Pattern.CASE_INSENSITIVE),
            Pattern.compile("С собой\\s*·", Pattern.CASE_INSENSITIVE),
            Pattern.compile("Доставка:\\s*Самовывоз", Pattern.CASE_INSENSITIVE),
            Pattern.compile("Пользователь заберет самостоятельно", Pattern.CASE_INSENSITIVE)
        };
        
        // Сначала проверяем самовывоз (более специфичные паттерны)
        for (Pattern pattern : pickupPatterns) {
            if (pattern.matcher(text).find()) {
                return OrderType.PICKUP;
            }
        }
        
        // Затем проверяем доставку
        for (Pattern pattern : deliveryPatterns) {
            if (pattern.matcher(text).find()) {
                return OrderType.DELIVERY;
            }
        }
        
        // По умолчанию - самовывоз
        return OrderType.PICKUP;
    }

    private String parseCustomerPhone(String text) {
        // Паттерны для телефона
        Pattern[] patterns = {
            Pattern.compile("Телефон:\\s*([+\\d\\s\\(\\)\\-]+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("Пользователь\\s*—\\s*Client\\s*([+\\d\\s\\(\\)\\-]+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\+7\\s*\\d{3}\\s*\\d{3}\\s*\\d{2}\\s*\\d{2}"),
            Pattern.compile("\\+79\\d{9}"),
            Pattern.compile("8\\s*\\d{3}\\s*\\d{3}\\s*\\d{2}\\s*\\d{2}")
        };
        
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                String phone = matcher.group(matcher.groupCount()).trim();
                // Убираем доп. номера типа "доб. 34931"
                phone = phone.replaceAll("\\s*доб\\..*$", "").trim();
                // Нормализуем формат
                phone = phone.replaceAll("[\\s\\(\\)\\-]", "");
                return phone;
            }
        }
        
        return null;
    }

    private PaymentType parsePaymentType(String text) {
        // Паттерны для типа оплаты
        Pattern[] cashlessPatterns = {
            Pattern.compile("🟢\\s*Оплачено онлайн", Pattern.CASE_INSENSITIVE),
            Pattern.compile("Оплата\\s*—\\s*безналичный платеж", Pattern.CASE_INSENSITIVE),
            Pattern.compile("Оплата\\s*Безналичн\\s*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("Оплата:\\s*оплачено онлайн", Pattern.CASE_INSENSITIVE),
            Pattern.compile("Оплата:\\s*оплачен онлайн", Pattern.CASE_INSENSITIVE)
        };
        
        Pattern[] cashPatterns = {
            Pattern.compile("Оплата:\\s*наличные", Pattern.CASE_INSENSITIVE),
            Pattern.compile("Оплата\\s*—\\s*наличные", Pattern.CASE_INSENSITIVE),
            Pattern.compile("💵Наличными курьеру:", Pattern.CASE_INSENSITIVE),
            Pattern.compile("Наличными курьеру:", Pattern.CASE_INSENSITIVE)
        };
        
        Pattern[] cardPatterns = {
            Pattern.compile("Оплата\\s*карт", Pattern.CASE_INSENSITIVE),
            Pattern.compile("Оплата\\s*—\\s*карта", Pattern.CASE_INSENSITIVE),
            Pattern.compile("Оплата:\\s*картой", Pattern.CASE_INSENSITIVE),
            Pattern.compile("💳Картой курьеру:", Pattern.CASE_INSENSITIVE),
            Pattern.compile("Картой курьеру:", Pattern.CASE_INSENSITIVE)
        };
        
        for (Pattern pattern : cashlessPatterns) {
            if (pattern.matcher(text).find()) {
                return PaymentType.CASHLESS;
            }
        }
        
        for (Pattern pattern : cashPatterns) {
            if (pattern.matcher(text).find()) {
                return PaymentType.CASH;
            }
        }
        
        for (Pattern pattern : cardPatterns) {
            if (pattern.matcher(text).find()) {
                return PaymentType.CARD;
            }
        }
        
        // По умолчанию - безналичные
        return PaymentType.CASHLESS;
    }

    private OrderAddressDto parseAddress(String text) {
        OrderAddressDto.OrderAddressDtoBuilder builder = OrderAddressDto.builder();
        
        // Парсим адрес доставки
        // Формат 1: "Строительная улица, 2А, Ухта, 1 подъезд, кв. 2"
        // Формат 2: "Адрес доставки — посёлок Парголово, улица Шишкина, д. 303к1"
        // Формат 3: "Адрес доставкипосёлок Парголово..." (без пробела/тире)
        // Формат 4: "Доставка: посёлок Парголово, улица Михаила Дудина 25к2"
        // Формат 5: "улица Дзержинского, 6, 2 подъезд, 18 домофон, 2 этаж, кв. 18"
        
        Pattern addressPattern = Pattern.compile(
            "(?:Адрес доставки\\s*[—-]?\\s*|Доставка:\\s*)([^\\n]+)",
            Pattern.CASE_INSENSITIVE
        );
        Matcher addressMatcher = addressPattern.matcher(text);
        
        String addressLine = null;
        if (addressMatcher.find()) {
            addressLine = addressMatcher.group(1).trim();
        } else {
            // Формат Starter: ищем строку после эмодзи времени (🕒К или ⏰Предзаказ)
            Pattern[] starterAddressPatterns = {
                Pattern.compile(
                    "🕒К\\s+\\d{1,2}:\\d{2}\\s*[–-]\\s*\\d{1,2}:\\d{2},\\s*\\d{2}\\.\\d{2}\\.\\d{4}\\s*\\n([^\\n]+)",
                    Pattern.CASE_INSENSITIVE
                ),
                Pattern.compile(
                    "⏰Предзаказ\\s+к\\s+\\d{1,2}:\\d{2}\\s*[–-]\\s*\\d{1,2}:\\d{2},\\s*\\d{2}\\.\\d{2}\\.\\d{4}\\s*\\n([^\\n]+)",
                    Pattern.CASE_INSENSITIVE
                )
            };
            
            for (Pattern pattern : starterAddressPatterns) {
                Matcher starterMatcher = pattern.matcher(text);
                if (starterMatcher.find()) {
                    addressLine = starterMatcher.group(1).trim();
                    break;
                }
            }
        }
        
        if (addressLine != null) {
            // Парсим компоненты адреса
            // Формат может быть:
            // 1. "город, улица, дом" или "посёлок город, улица, дом"
            // 2. "улица название, номер дома, доп. инфо..."
            // 3. "проспект X, дом, город" — формат Starter: "улица, дом, город"
            // 4. "Советская улица 13" (без запятых)
            String[] parts = addressLine.split(",");
            
            if (parts.length >= 2) {
                // Первая часть - может быть город или улица (тип в начале: "улица X", или в конце: "X улица")
                String firstPart = parts[0].trim();
                boolean isStreetPrefix = firstPart.toLowerCase().matches("^(улица|проспект|пр\\.|переулок|пер\\.|бульвар|шоссе)\\s+.+");
                boolean isStreetSuffix = firstPart.toLowerCase().matches(".+\\s+(улица|проспект|пр\\.|переулок|пер\\.|бульвар|шоссе)\\s*$");
                
                // Если тип улицы в начале или в конце — это улица, второй токен — дом, город из текста (Кухня Парнас и т.д.)
                if (isStreetPrefix || isStreetSuffix) {
                    builder.street(firstPart);
                    String house = parts[1].trim().split("\\s+")[0];
                    builder.house(house);
                    String city = parseCity(text);
                    if (city != null) builder.city(city);
                } else if (parts.length >= 4 && parts[2].trim().equalsIgnoreCase("посёлок") && isKnownCity(parts[3].trim()) && looksLikeHouseNumber(parts[1].trim())) {
                    // Формат: "Заречная улица, 42к1, посёлок, Парголово" — запятая между посёлок и городом
                    builder.street(firstPart);
                    builder.house(parts[1].trim().split("\\s+")[0]);
                    builder.city(extractCityFromPart(parts[3].trim()));
                } else if (parts.length >= 3 && isKnownCity(parts[2].trim()) && looksLikeHouseNumber(parts[1].trim())) {
                    // Формат Starter: "проспект Космонавтов, 12, Ухта" — улица, дом, город
                    builder.street(firstPart.trim());
                    
                    String house = parts[1].trim().split("\\s+")[0];
                    builder.house(house);
                    
                    builder.city(parts[2].trim());
                } else if (parts.length >= 3 && looksLikeHouseNumber(parts[1].trim()) && isCityPart(parts[2].trim())) {
                    // Формат: "улица/проезд, дом, посёлок город" — например "Толубеевский проезд, 26к1, посёлок Парголово"
                    String street = firstPart.replaceAll("^улица\\s+", "");
                    builder.street(street);
                    String house = parts[1].trim().split("\\s+")[0];
                    builder.house(house);
                    String city = extractCityFromPart(parts[2].trim());
                    builder.city(city);
                } else if (parts.length >= 3 && (firstPart.toLowerCase().startsWith("посёлок") || isKnownCity(firstPart))) {
                    // Формат: "город, улица, дом" или "посёлок город, улица, дом"
                    String city = firstPart.replaceAll("^посёлок\\s+", "");
                    builder.city(city);
                    
                    String street = parts[1].trim().replaceAll("^улица\\s+", "");
                    builder.street(street);
                    
                    String house = parts[2].trim()
                            .replaceAll("^д\\.\\s*", "")
                            .replaceAll("^дом\\s+", "");
                    house = house.split("\\s+")[0];
                    builder.house(house);
                } else if (parts.length == 2 && looksLikeHouseNumber(parts[1].trim())) {
                    // Формат из двух частей: "улица/проезд, дом" (город возьмём из текста через parseCity)
                    String street = firstPart.replaceAll("^улица\\s+", "");
                    builder.street(street);
                    builder.house(parts[1].trim().split("\\s+")[0]);
                    String city = parseCity(text);
                    if (city != null) builder.city(city);
                } else if (parts.length >= 3) {
                    // Fallback: "город, улица, дом" (если не определили формат Starter)
                    String city = firstPart.replaceAll("^посёлок\\s+", "");
                    builder.city(city);
                    
                    String street = parts[1].trim().replaceAll("^улица\\s+", "");
                    builder.street(street);
                    
                    String house = parts[2].trim()
                            .replaceAll("^д\\.\\s*", "")
                            .replaceAll("^дом\\s+", "");
                    house = house.split("\\s+")[0];
                    builder.house(house);
                }
            } else if (parts.length == 1) {
                // Формат без запятых: "Советская улица 13", "улица Советская 13"
                String singleLine = parts[0].trim();
                // Ищем номер дома в конце (цифры, возможно с буквами: 13, 13к1, 2А)
                Pattern streetHousePattern = Pattern.compile("(.+?)\\s+(\\d+[a-zA-Zа-яА-ЯкК]*)\\s*$");
                Matcher m = streetHousePattern.matcher(singleLine);
                if (m.find()) {
                    String street = m.group(1).trim().replaceAll("^улица\\s+", "");
                    builder.street(street);
                    builder.house(m.group(2));
                } else {
                    builder.street(singleLine.replaceAll("^улица\\s+", ""));
                }
                String city = parseCity(text);
                if (city != null) builder.city(city);
            }
        }
        
        // Парсим квартиру (Квартира: 62 или кв. 62)
        Pattern flatPattern = Pattern.compile("(?:кв\\.|квартира)[:\\s]*(\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher flatMatcher = flatPattern.matcher(text);
        if (flatMatcher.find()) {
            builder.flat(flatMatcher.group(1));
        }
        
        // Парсим подъезд (15 подъезд или Подъезд: 15)
        Pattern entrancePattern = Pattern.compile("(?:подъезд[:\\s]+(\\d+)|(\\d+)\\s+подъезд)", Pattern.CASE_INSENSITIVE);
        Matcher entranceMatcher = entrancePattern.matcher(text);
        if (entranceMatcher.find()) {
            String entrance = entranceMatcher.group(1) != null ? entranceMatcher.group(1) : entranceMatcher.group(2);
            builder.entrance(entrance);
        }
        
        // Парсим этаж (1 этаж или Этаж: 1)
        Pattern floorPattern = Pattern.compile("(?:этаж[:\\s]+(\\d+)|(\\d+)\\s+этаж)", Pattern.CASE_INSENSITIVE);
        Matcher floorMatcher = floorPattern.matcher(text);
        if (floorMatcher.find()) {
            String floor = floorMatcher.group(1) != null ? floorMatcher.group(1) : floorMatcher.group(2);
            builder.floor(floor);
        }
        
        // Парсим домофон
        Pattern doorphonePattern = Pattern.compile("(\\d+)\\s+домофон", Pattern.CASE_INSENSITIVE);
        Matcher doorphoneMatcher = doorphonePattern.matcher(text);
        if (doorphoneMatcher.find()) {
            builder.doorphone(doorphoneMatcher.group(1));
        }
        
        return builder.build();
    }
}

