package ru.reik.smarthome.orchestrator.dto.smarthome;

import java.util.Map;

public record SmartHomeAction(
        String code,
        String title,
        String haDomain,
        String haService,
        Map<String, Object> defaultPayload,
        Map<String, ActionParameterDefinition> parameters
) {
    public SmartHomeAction {
        defaultPayload = defaultPayload == null
                ? Map.of()
                : Map.copyOf(defaultPayload);

        parameters = parameters == null
                ? Map.of()
                : Map.copyOf(parameters);
    }
}
