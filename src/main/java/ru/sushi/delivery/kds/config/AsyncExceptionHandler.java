package ru.sushi.delivery.kds.config;

import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Log4j2
@Component
public class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

    @Override
    public void handleUncaughtException(Throwable ex, Method method, Object... params) {
        log.error("Async method {} failed with exception: {}", method.getName(), ex.getMessage(), ex);
        
        // Можно добавить дополнительную логику обработки ошибок
        // например, отправку уведомлений, запись в базу данных и т.д.
    }
}
