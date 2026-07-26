package ru.reik.smarthome.orchestrator.dto.llm;

public record LlmEnvironmentContext(
        String localDate,
        String localTime,
        String dayOfWeek
) {
}
