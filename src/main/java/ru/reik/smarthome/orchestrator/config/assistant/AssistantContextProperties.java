package ru.reik.smarthome.orchestrator.config.assistant;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.ZoneId;

@Validated
@ConfigurationProperties(prefix = "assistant.context")
public record AssistantContextProperties(
        String timeZone
) {
    public AssistantContextProperties {
        if (timeZone == null || timeZone.isBlank()) {
            throw new IllegalArgumentException("assistant.context.time-zone must not be blank");
        }

        /*
         * Сразу проверяем корректность значения.
         * При ошибке приложение не должно тихо использовать
         * случайный часовой пояс сервера.
         */
        ZoneId.of(timeZone);
    }
}
