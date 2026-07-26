package ru.reik.smarthome.orchestrator.dto.llm;

public record LlmConversationTurn(
        String userMessage,
        String assistantResponse,
        String executionResult
) {
}
