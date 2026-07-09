package ru.reik.smarthome.orchestrator.dto.assistant;

import java.util.List;

public record AssistantResponse(
        String answer,
        List<AssistantAction> actions
) {
    public AssistantResponse {
        actions = actions == null ? List.of() : List.copyOf(actions);
    }

    public static AssistantResponse text(String answer) {
        return new AssistantResponse(answer, List.of());
    }
}
