package ru.reik.smarthome.orchestrator.config.llm;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "llm")
public record LlmProperties(
        boolean enabled,
        @NotBlank String baseUrl,
        @NotBlank String apiKey,
        @NotBlank String model
) {
    public void validateConfigured() {
        if (!enabled) {
            throw new IllegalStateException(
                    "LLM-интеграция отключена. Установи LLM_ENABLED=true"
            );
        }

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "Не задана переменная окружения LLM_API_KEY"
            );
        }
    }
}
