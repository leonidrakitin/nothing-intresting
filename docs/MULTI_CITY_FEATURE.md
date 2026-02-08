# Функциональность мультигородов

## Описание

Система поддерживает работу с несколькими городами (филиалами). Каждый город имеет свою отдельную базу данных с собственным меню, заказами и настройками.

## Поддерживаемые города

1. **Парнас** - основной филиал
2. **Ухта** - дополнительный филиал

## Архитектура

### Компоненты системы

#### 1. MultiCityViewService
Сервис для чтения данных из разных городов:
- `getMenuItems(City city)` - получение меню города
- `getExtras(City city)` - получение дополнительных позиций
- `getCombos(City city)` - получение комбо-наборов

#### 2. MultiCityOrderService
Сервис для работы с заказами в разных городах:
- `createOrder(City city, ...)` - создание заказа в выбранном городе
- `orderExistsByNameToday(City city, String name)` - проверка существования заказа
- `getAllActiveOrdersWithItems(City city)` - получение активных заказов
- `updateKitchenShouldGetOrderAt(City city, ...)` - обновление времени заказа
- `cancelOrder(City city, Long orderId)` - отмена заказа
- `addItemToOrder(City city, ...)` - добавление позиции в заказ

#### 3. CreateNewOrderView
UI для создания заказов с поддержкой мультигородов:
- Переключение между городами через вкладки
- Визуальная индикация текущего выбранного города
- Автоматическая загрузка меню при смене города
- Подтверждение при смене города с товарами в корзине

## Конфигурация

### Настройка источников данных

Система использует три `JdbcTemplate`:

1. **Основной JdbcTemplate** (`@Primary`) - для работы с основной БД через JPA
2. **parnasJdbcTemplate** - для работы с БД города Парнас
3. **ukhtaJdbcTemplate** - для работы с БД города Ухта

Конфигурация в `PrimaryDataSourceConfig`:
```java
@Bean
@Primary
public JdbcTemplate jdbcTemplate(DataSource primaryDataSource) {
    return new JdbcTemplate(primaryDataSource);
}
```

Конфигурация в `MultiCityDataSourceConfig`:
```java
@Bean
@Qualifier("parnasJdbcTemplate")
public JdbcTemplate parnasJdbcTemplate(@Qualifier("parnasDataSource") DataSource dataSource) {
    return new JdbcTemplate(dataSource);
}

@Bean
@Qualifier("ukhtaJdbcTemplate")
public JdbcTemplate ukhtaJdbcTemplate(@Qualifier("ukhtaDataSource") DataSource dataSource) {
    return new JdbcTemplate(dataSource);
}
```

**Важно:** Основной `JdbcTemplate` помечен как `@Primary`, поэтому все репозитории без явного `@Qualifier` будут использовать его. Для работы с мультигородами используются специальные сервисы с явным указанием нужного `JdbcTemplate`.

### Настройка подключений

Параметры подключения к БД городов настраиваются в `application.properties` или через переменные окружения:

```properties
# Парнас
spring.datasource.parnas.url=jdbc:postgresql://localhost:5432/kds_parnas
spring.datasource.parnas.username=...
spring.datasource.parnas.password=...

# Ухта
spring.datasource.ukhta.url=jdbc:postgresql://localhost:5432/kds_ukhta
spring.datasource.ukhta.username=...
spring.datasource.ukhta.password=...
```

## Использование

### Создание заказа

1. Откройте страницу создания заказа: `/create-new`
2. Выберите нужный город через вкладки в верхней части страницы
3. Добавьте товары в корзину
4. Заполните данные заказа
5. Нажмите "Создать заказ"

**Важно:** Заказ будет создан в базе данных выбранного города!

### Визуальные индикаторы

- **Заголовок страницы** - показывает текущий выбранный город с цветовой кодировкой:
  - Парнас: синий цвет (#1976D2)
  - Ухта: зелёный цвет (#388E3C)

- **Индикатор в корзине** - бейдж с названием города рядом с заголовком "Корзина"

### Переключение города

При переключении города:
1. Если корзина пуста - переключение происходит мгновенно
2. Если в корзине есть товары - система запросит подтверждение, так как корзина будет очищена
3. После переключения загружается меню нового города
4. Обновляются все таблицы с заказами

## Безопасность

### Изоляция данных

- Каждый город работает с отдельной базой данных
- Данные городов полностью изолированы друг от друга
- Невозможно случайно создать заказ не в том городе

### Проверки

- Проверка существования заказа с таким же номером в текущем городе
- Валидация данных перед сохранением
- Логирование всех операций с указанием города

## Расширение системы

### Добавление нового города

1. Добавьте новый enum в `MultiCityViewService.City` и `MultiCityOrderService.City`:
```java
public enum City {
    PARNAS,
    UKHTA,
    NEW_CITY  // новый город
}
```

2. Создайте новый `JdbcTemplate` в конфигурации:
```java
@Bean
@Qualifier("newCityJdbcTemplate")
public JdbcTemplate newCityJdbcTemplate(@Qualifier("newCityDataSource") DataSource dataSource) {
    return new JdbcTemplate(dataSource);
}
```

3. Добавьте новый case в методы `getTemplate()`:
```java
private JdbcTemplate getTemplate(City city) {
    return switch (city) {
        case PARNAS -> parnasJdbcTemplate;
        case UKHTA -> ukhtaJdbcTemplate;
        case NEW_CITY -> newCityJdbcTemplate;
    };
}
```

4. Добавьте новую вкладку в UI (`CreateNewOrderView`):
```java
Tab newCityTab = new Tab("Новый город");
Tabs cityTabs = new Tabs(parnasTab, ukhtaTab, newCityTab);
```

## Мониторинг и логирование

Все операции логируются с указанием города:
```
INFO: Order created: 123 in city: Парнас with 5 items
INFO: Switched to city: Ухта
ERROR: Error loading data for city: Парнас
```

## Известные ограничения

1. Нельзя перенести заказ из одного города в другой
2. Меню городов управляется независимо
3. Статистика считается отдельно для каждого города

## Поддержка

При возникновении проблем проверьте:
1. Подключение к базам данных обоих городов
2. Логи приложения на наличие ошибок
3. Правильность настроек в `application.properties`
