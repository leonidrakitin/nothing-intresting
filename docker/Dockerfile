# Базовый образ для Java
FROM openjdk:21-jdk-slim

# Установить рабочую директорию
WORKDIR /app

# Копируем JAR-файл в контейнер
COPY build/libs/your-app-name.jar app.jar

# Открываем порт (по умолчанию Vaadin/Spring Boot используют 8080)
EXPOSE 8080

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "app.jar"]
