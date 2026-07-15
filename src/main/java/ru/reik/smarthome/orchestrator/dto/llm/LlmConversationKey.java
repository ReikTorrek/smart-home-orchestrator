package ru.reik.smarthome.orchestrator.dto.llm;

import ru.reik.smarthome.orchestrator.dto.assistant.AssistantClientType;

public record LlmConversationKey(
        AssistantClientType clientType,
        String conversationId
) {
    public LlmConversationKey {
        if (clientType == null) {
            throw new IllegalArgumentException("clientType cannot be null");
        }

        if (conversationId == null || conversationId.isBlank()) {
            throw new IllegalArgumentException("conversationId cannot be blank");
        }
    }
}
