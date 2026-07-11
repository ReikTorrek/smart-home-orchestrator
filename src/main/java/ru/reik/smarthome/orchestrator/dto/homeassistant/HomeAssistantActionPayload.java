package ru.reik.smarthome.orchestrator.dto.homeassistant;

import java.util.Map;

public record HomeAssistantActionPayload(
        String entityId,
        String actionCode,
        Map<String, Object> parameters
) {
    public HomeAssistantActionPayload {
        if (entityId == null || entityId.isBlank()) {
            throw new IllegalArgumentException(
                    "entityId must not be blank"
            );
        }

        if (actionCode == null || actionCode.isBlank()) {
            throw new IllegalArgumentException(
                    "actionCode must not be blank"
            );
        }

        parameters = parameters == null
                ? Map.of()
                : Map.copyOf(parameters);
    }
}
