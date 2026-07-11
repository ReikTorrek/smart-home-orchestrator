package ru.reik.smarthome.orchestrator.dto.assistant;

import ru.reik.smarthome.orchestrator.dto.homeassistant.HomeAssistantActionPayload;

import java.util.List;

public record AssistantResponse(
        String answer,
        List<HomeAssistantActionPayload> actions
) {
    public AssistantResponse {
        answer = answer == null ? "" : answer;
        actions = actions == null ? List.of() : List.copyOf(actions);
    }

    public static AssistantResponse text(String answer) {
        return new AssistantResponse(answer, List.of());
    }

    public static AssistantResponse withAction(String answer, HomeAssistantActionPayload action) {
        return new AssistantResponse(answer, List.of(action));
    }

    public static  AssistantResponse withActions(String answer, List<HomeAssistantActionPayload> actions) {
        return new AssistantResponse(answer, actions);
    }
}
