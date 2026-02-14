# Stage 1: build
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Gradle wrapper + конфиг
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Скачивание зависимостей (отдельный слой для кэша)
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon

# Исходники
COPY src src

# Сборка snapshot (production mode для Vaadin)
RUN ./gradlew bootJar -Pvaadin.production --no-daemon -x test

# Stage 2: run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/kds-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["sh", "-c", "java -Duser.timezone=Europe/Moscow -Dserver.port=${SERVER_PORT} -Dspring.profiles.active=prod -jar app.jar"]