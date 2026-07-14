package ru.reik.smarthome.orchestrator.dto.assistant;

public record AssistantReply(
        String text
) {
    public AssistantReply {
        text = text == null ? "" : text;
    }
}
