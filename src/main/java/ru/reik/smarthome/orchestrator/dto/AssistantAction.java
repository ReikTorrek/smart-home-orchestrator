package ru.reik.smarthome.orchestrator.dto;

import java.util.Map;
import java.util.Objects;

public record AssistantAction(
        String type,
        Map<String, Object> payload
) {
}
