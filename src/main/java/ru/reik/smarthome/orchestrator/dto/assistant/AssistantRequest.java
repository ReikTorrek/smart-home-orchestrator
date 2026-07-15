package ru.reik.smarthome.orchestrator.dto.assistant;

public record AssistantRequest(
        AssistantClientType clientType,
        String conversationId,
        String text
) {
    public AssistantRequest {
        if (clientType == null) {
            throw new IllegalStateException("clientType is null");
        }

        if (conversationId == null || conversationId.isBlank()) {
            throw new IllegalArgumentException(
                    "conversationId must not be blank"
            );
        }

        text = text == null ? "" : text;
    }
}
