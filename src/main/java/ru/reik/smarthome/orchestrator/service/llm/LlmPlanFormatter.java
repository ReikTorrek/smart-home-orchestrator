package ru.reik.smarthome.orchestrator.service.llm;

import org.springframework.stereotype.Component;
import ru.reik.smarthome.orchestrator.dto.assistant.AssistantResponse;
import ru.reik.smarthome.orchestrator.dto.homeassistant.HomeAssistantActionPayload;

import java.util.stream.Collectors;

@Component
public class LlmPlanFormatter {
    public String format(
            AssistantResponse plan
    ) {
        if (plan.actions().isEmpty()) {
            return """
                    Ответ модели:
                    %s

                    Действия не выбраны.
                    План не выполнялся.
                    """.formatted(plan.answer());
        }

        String actions = plan.actions().stream()
                .map(this::formatAction)
                .collect(Collectors.joining("\n"));

        return """
                Ответ модели:
                %s

                Выбранные действия:
                %s

                План не выполнялся.
                """.formatted(
                plan.answer(),
                actions
        );
    }

    private String formatAction(
            HomeAssistantActionPayload action
    ) {
        return "- %s → %s %s".formatted(
                action.entityId(),
                action.actionCode(),
                action.parameters()
        );
    }
}
