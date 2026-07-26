package ru.reik.smarthome.orchestrator.dto.llm;

import java.util.List;

public record LlmExecutionResult(
        String status,
        long successCount,
        long failureCount,
        List<ActionResult> actions
) {
    public record ActionResult(
            String entityId,
            String actionCode,
            boolean successful,
            String error
    ) {
    }
}
