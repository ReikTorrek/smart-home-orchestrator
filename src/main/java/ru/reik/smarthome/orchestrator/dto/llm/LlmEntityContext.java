package ru.reik.smarthome.orchestrator.dto.llm;

import ru.reik.smarthome.orchestrator.dto.smarthome.SmartHomeAction;

import java.util.List;
import java.util.Map;

public record LlmEntityContext(
        String entityId,
        String domain,
        String name,
        String state,
        Map<String, Object> currentValues,
        List<SmartHomeAction> actions
) {
    public LlmEntityContext {
        currentValues = currentValues == null
                ? Map.of()
                : Map.copyOf(currentValues);

        actions = actions == null
                ? List.of()
                : List.copyOf(actions);
    }
}
