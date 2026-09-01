package ru.reik.smarthome.orchestrator.config.assistant;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "assistant.api")
public record AssistantApiProperties(
        String authToken
) {
    public AssistantApiProperties {
        if (authToken == null || authToken.isBlank()) {
            throw new IllegalArgumentException("assistant.context.api.token must not be blank");
        }
    }
}
