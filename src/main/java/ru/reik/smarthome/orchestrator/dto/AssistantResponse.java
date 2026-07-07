package ru.reik.smarthome.orchestrator.dto;

import java.util.List;

public record AssistantResponse(
        String answer,
        List<AssistantAction> actions
) {
}
