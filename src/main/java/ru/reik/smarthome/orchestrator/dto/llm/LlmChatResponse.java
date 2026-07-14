package ru.reik.smarthome.orchestrator.dto.llm;

import java.util.List;

public record LlmChatResponse(
        String model,
        List<Choice> choices
) {
    public record Choice(
            Message message
    ) {
    }

    public record Message(
            String role,
            String content
    ) {
    }
}
