package ru.reik.smarthome.orchestrator.dto.assistant;

public record AssistantRequest(
        AssistantClientType clientType,
        String text
) {
    public AssistantRequest {
        if (clientType == null) {
            throw new IllegalStateException("clientType is null");
        }

        text = text == null ? "" : text;
    }
}
