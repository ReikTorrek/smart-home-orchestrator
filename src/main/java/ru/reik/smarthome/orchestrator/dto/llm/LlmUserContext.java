package ru.reik.smarthome.orchestrator.dto.llm;

import java.util.List;

public record LlmUserContext(
        String command,
        LlmEnvironmentContext environment,
        List<LlmEntityContext> entities
) {
}
