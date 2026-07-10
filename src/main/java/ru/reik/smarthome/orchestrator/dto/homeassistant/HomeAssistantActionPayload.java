package ru.reik.smarthome.orchestrator.dto.homeassistant;

import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

@Validated
public record HomeAssistantActionPayload(
        @NotBlank String entityId,
        @NotBlank String actionCode,
        Map<String, Object> parameters
) {
    public HomeAssistantActionPayload {
        parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
    }
}
