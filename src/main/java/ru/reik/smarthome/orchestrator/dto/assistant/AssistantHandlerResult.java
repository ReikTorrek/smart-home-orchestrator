package ru.reik.smarthome.orchestrator.dto.assistant;

public record AssistantHandlerResult(
        AssistantResponse response,
        ConversationPolicy policy
) {
    public AssistantHandlerResult {
        if (response == null) {
            throw new NullPointerException("response is null");
        }
        if (policy == null) {
            policy = ConversationPolicy.SKIP;
        }
    }

    public static AssistantHandlerResult save(AssistantResponse response) {
        return new AssistantHandlerResult(response, ConversationPolicy.SAVE);
    }

    public static AssistantHandlerResult skip(AssistantResponse response) {
        return new AssistantHandlerResult(response, ConversationPolicy.SKIP);
    }
}
