# Решение проблем (Troubleshooting)

## Vaadin: Unable to find index.html (production mode)

**Полный текст ошибки:**
```
java.io.IOException: Unable to find index.html. It should be available on the classpath when running in production mode
```

**Причина:** В production mode Vaadin ожидает предсобранный frontend, который создаётся при `./gradlew build`. При запуске через `./gradlew bootRun` сборка frontend не выполняется.

**Решение:**

1. **Для разработки** — используйте production mode = false (уже настроено по умолчанию):
   - `./gradlew bootRun` — работает без дополнительной сборки

2. **Для продакшена** — соберите проект:
   ```bash
   ./gradlew build -Pvaadin.production
   java -jar build/libs/kds-system-*.jar
   ```

3. **Через переменную окружения** (для деплоя):
   ```bash
   VAADIN_PRODUCTION_MODE=true java -jar build/libs/kds-system-*.jar
   ```

---

## Ошибка 500 (Internal Server Error)

**Симптомы:**
- Whitelabel Error Page
- "There was an unexpected error (type=Internal Server Error, status=500)"

**Как узнать причину:**

1. **Включен вывод деталей ошибки** в `application.yaml`:
   ```yaml
   server:
     error:
       include-message: always
       include-stacktrace: on_param
   ```
   После перезапуска приложения сообщение об ошибке будет видно на странице.

2. **Добавьте `?trace=true`** к URL, где возникает ошибка — отобразится полный stacktrace.

3. **Проверьте логи приложения** — там должна быть полная информация:
   ```bash
   tail -f logs/application.log
   # или
   ./gradlew bootRun 2>&1 | tee run.log
   ```

**Частые причины 500:**
- Недоступна БД (основная или parnas/ukhta)
- Отсутствующие таблицы/колонки в БД
- NPE при загрузке страницы (например, CreateNewOrderView при обращении к БД городов)

---

## Проблемы с мультигородами

### Ошибка: "required a single bean, but 2 were found"

**Полный текст ошибки:**
```
Parameter 0 of constructor in ru.sushi.delivery.kds.domain.persist.repository.OrderTimelineRepository 
required a single bean, but 2 were found:
	- parnasJdbcTemplate
	- ukhtaJdbcTemplate
```

**Причина:**
После добавления мультигородов появилось несколько `JdbcTemplate` бинов, и Spring не знает, какой использовать для репозиториев, которые работают с основной БД.

**Решение:**
Создан основной `JdbcTemplate` с аннотацией `@Primary` в `PrimaryDataSourceConfig`:

```java
@Bean
@Primary
public JdbcTemplate jdbcTemplate(DataSource primaryDataSource) {
    return new JdbcTemplate(primaryDataSource);
}
```

Теперь:
- Репозитории без `@Qualifier` используют основной `JdbcTemplate` (основная БД)
- Сервисы мультигородов используют `parnasJdbcTemplate` и `ukhtaJdbcTemplate` с явным `@Qualifier`

---

## Проблемы с подключением к БД

### Не удаётся подключиться к БД города

**Симптомы:**
- Ошибка при переключении города
- Сообщение "Ошибка загрузки данных для города..."

**Проверьте:**
1. Правильность настроек в `application.properties`:
   ```properties
   parnas.datasource.url=jdbc:postgresql://host:port/database
   parnas.datasource.username=username
   parnas.datasource.password=password
   ```

2. Доступность сервера БД:
   ```bash
   psql -h host -p port -U username -d database
   ```

3. Логи приложения на наличие `SQLException`

**Решение:**
- Проверьте сетевую доступность
- Убедитесь, что учётные данные верны
- Проверьте, что БД запущена

---

## Проблемы с созданием заказов

### Заказ создаётся не в том городе

**Симптомы:**
- Заказ не появляется в списке активных заказов выбранного города
- Заказ появляется в другом городе

**Причина:**
Возможно, произошло переключение города после добавления товаров в корзину.

**Решение:**
1. Всегда проверяйте индикатор города перед созданием заказа
2. Смотрите на цвет заголовка (синий = Парнас, зелёный = Ухта)
3. Проверяйте бейдж "Город: ..." в корзине

**Профилактика:**
Система показывает подтверждение при переключении города с товарами в корзине.

---

## Проблемы с производительностью

### Медленная загрузка меню при переключении города

**Причина:**
Большой объём данных в меню города или медленное подключение к БД.

**Решение:**
1. Проверьте скорость подключения к БД:
   ```sql
   SELECT COUNT(*) FROM menu_item;
   SELECT COUNT(*) FROM item_combo;
   ```

2. Проверьте индексы в БД:
   ```sql
   -- Должны быть индексы на часто используемых полях
   CREATE INDEX IF NOT EXISTS idx_menu_item_name ON menu_item(name);
   CREATE INDEX IF NOT EXISTS idx_item_combo_name ON item_combo(name);
   ```

3. Увеличьте размер пула подключений в `MultiCityDataSourceConfig`:
   ```java
   config.setMaximumPoolSize(20); // вместо 10
   ```

---

## Проблемы с логированием

### Не видно логов переключения города

**Проверьте уровень логирования:**
```properties
logging.level.ru.sushi.delivery.kds.view=INFO
logging.level.ru.sushi.delivery.kds.service=INFO
```

**Должны появиться логи:**
```
INFO: Switched to city: Парнас
INFO: Order created: 123 in city: Ухта with 5 items
```

---

## Известные ограничения

### 1. Нельзя перенести заказ между городами
**Обходной путь:** Отмените заказ в одном городе и создайте заново в другом.

### 2. Статистика не агрегируется между городами
**Обходной путь:** Используйте отдельные отчёты для каждого города.

### 3. Меню городов управляется независимо
**Это не баг, а фича:** Каждый город может иметь свой ассортимент.

---

## Получение помощи

Если проблема не решена:

1. Проверьте логи приложения:
   ```bash
   tail -f logs/application.log
   ```

2. Включите debug-логирование:
   ```properties
   logging.level.ru.sushi.delivery.kds=DEBUG
   ```

3. Обратитесь к системному администратору с:
   - Описанием проблемы
   - Логами ошибок
   - Шагами для воспроизведения
