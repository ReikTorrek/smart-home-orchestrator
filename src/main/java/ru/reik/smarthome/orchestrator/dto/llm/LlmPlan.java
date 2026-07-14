package ru.reik.smarthome.orchestrator.dto.llm;

import java.util.List;
import java.util.Map;

public record LlmPlan(
        String answer,
        List<Action> actions
) {
    public LlmPlan {
        actions = actions == null
                ? List.of()
                : List.copyOf(actions);
    }

    public record Action(
            String entityId,
            String actionCode,
            Map<String, Object> parameters
    ) {
        public Action {
            parameters = parameters == null
                    ? Map.of()
                    : Map.copyOf(parameters);
        }
    }
}
