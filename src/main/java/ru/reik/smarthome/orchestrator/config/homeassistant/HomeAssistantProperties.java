package ru.reik.smarthome.orchestrator.config.homeassistant;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "home-assistant")
public record HomeAssistantProperties(
        @NotBlank String longLivedToken,
        @NotBlank String baseUrl,
        @NotBlank String testEntityId
) {
}
