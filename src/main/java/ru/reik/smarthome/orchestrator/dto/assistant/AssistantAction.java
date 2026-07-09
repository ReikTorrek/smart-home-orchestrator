package ru.reik.smarthome.orchestrator.dto.assistant;

import java.util.Map;

public record AssistantAction(
        String type,
        Map<String, Object> payload
) {
}
