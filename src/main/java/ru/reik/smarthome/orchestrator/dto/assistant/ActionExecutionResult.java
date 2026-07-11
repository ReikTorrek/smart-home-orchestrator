package ru.reik.smarthome.orchestrator.dto.assistant;

import ru.reik.smarthome.orchestrator.dto.homeassistant.HomeAssistantActionPayload;

public record ActionExecutionResult(
        HomeAssistantActionPayload payload,
        boolean success,
        String error
) {
    public static ActionExecutionResult success(HomeAssistantActionPayload payload) {
        return new ActionExecutionResult(payload, true, null);
    }

    public static ActionExecutionResult failure(HomeAssistantActionPayload payload, String error) {
        return new ActionExecutionResult(payload, false, error);
    }
}
