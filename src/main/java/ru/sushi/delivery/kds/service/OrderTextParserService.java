package ru.sushi.delivery.kds.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.persist.entity.ItemCombo;
import ru.sushi.delivery.kds.domain.persist.entity.product.MenuItem;
import ru.sushi.delivery.kds.dto.ParsedOrderDto;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
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

@Slf4j
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
        
        // Парсим комментарий
        String comment = parseComment(text);
        builder.comment(comment);
        
        // Парсим время начала и готовности
        Instant kitchenStartTime = parseKitchenStartTime(text);
        builder.kitchenStartTime(kitchenStartTime);
        Instant finishTime = parseFinishTime(text);
        builder.finishTime(finishTime);
        
        // Парсим приборы
        Integer instrumentsCount = parseInstrumentsCount(text);
        builder.instrumentsCount(instrumentsCount);
        
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
                    LocalDate date = LocalDate.now(); // Используем текущую дату
                    
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
                            date = LocalDate.now().withDayOfMonth(day);
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
                    LocalDate date = LocalDate.now();
                    
                    // Если есть день и месяц
                    if (matcher.groupCount() >= 2 && matcher.group(2) != null) {
                        try {
                            int day = Integer.parseInt(matcher.group(2));
                            date = LocalDate.now().withDayOfMonth(day);
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
        Set<String> processedPositions = new HashSet<>(); // Для отслеживания уже обработанных позиций
        
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
            if (processedPositions.contains(normalizedName)) {
                continue;
            }
            
            // Ищем соответствующий MenuItem
            MenuItem foundItem = findMenuItemByName(allMenuItems, name);
            
            // Добавляем позицию в список независимо от того, найдена она или нет
            items.add(ParsedOrderDto.ParsedItem.builder()
                .name(name)
                .quantity(quantity)
                .menuItem(foundItem) // Может быть null если не найдено
                .build());
            processedPositions.add(normalizedName);
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
            
            // Проверяем, не обрабатывали ли мы уже эту позицию
            if (processedPositions.contains(normalizedName)) {
                continue;
            }
            
            // Ищем соответствующий MenuItem
            MenuItem foundItem = findMenuItemByName(allMenuItems, name);
            
            // Добавляем позицию в список независимо от того, найдена она или нет
            items.add(ParsedOrderDto.ParsedItem.builder()
                .name(name)
                .quantity(quantity)
                .menuItem(foundItem) // Может быть null если не найдено
                .build());
            processedPositions.add(normalizedName);
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
            
            // Проверяем, не обрабатывали ли мы уже эту позицию
            if (processedPositions.contains(normalizedName)) {
                continue;
            }
            
            // Ищем соответствующий MenuItem
            MenuItem foundItem = findMenuItemByName(allMenuItems, name);
            
            // Добавляем позицию в список независимо от того, найдена она или нет
            items.add(ParsedOrderDto.ParsedItem.builder()
                .name(name)
                .quantity(quantity)
                .menuItem(foundItem) // Может быть null если не найдено
                .build());
            processedPositions.add(normalizedName);
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
            
            // Проверяем, не обрабатывали ли мы уже эту позицию
            if (processedPositions.contains(normalizedName)) {
                continue;
            }
            
            // Ищем соответствующий MenuItem
            MenuItem foundItem = findMenuItemByName(allMenuItems, name);
            
            // Добавляем позицию в список независимо от того, найдена она или нет
            items.add(ParsedOrderDto.ParsedItem.builder()
                .name(name)
                .quantity(quantity)
                .menuItem(foundItem) // Может быть null если не найдено
                .build());
            processedPositions.add(normalizedName);
        }
        
        // Универсальный паттерн для поиска всех позиций вида "1 x Название" или "1× Название" или "• 1 x Название"
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
            
            // Проверяем, не обрабатывали ли мы уже эту позицию
            if (processedPositions.contains(normalizedName)) {
                continue;
            }
            
            // Ищем соответствующий MenuItem
            MenuItem foundItem = findMenuItemByName(allMenuItems, name);
            
            // Добавляем позицию в список независимо от того, найдена она или нет
            items.add(ParsedOrderDto.ParsedItem.builder()
                .name(name)
                .quantity(quantity)
                .menuItem(foundItem) // Может быть null если не найдено
                .build());
            processedPositions.add(normalizedName);
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
}

