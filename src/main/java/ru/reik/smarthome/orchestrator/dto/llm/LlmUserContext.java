package ru.reik.smarthome.orchestrator.dto.llm;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

public record LlmUserContext(
        String command,
        LlmEnvironmentContext environment,
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        List<LlmEntityContext> entities
) {
}
