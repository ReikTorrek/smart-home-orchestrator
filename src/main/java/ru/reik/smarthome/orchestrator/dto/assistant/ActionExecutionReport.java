package ru.reik.smarthome.orchestrator.dto.assistant;

import java.util.List;

public record ActionExecutionReport(
        List<ActionExecutionResult> results
) {
    public ActionExecutionReport {
        results = results == null
                ? List.of()
                : List.copyOf(results);
    }

    public boolean isSuccessful() {
        return !hasFailures();
    }

    public boolean hasFailures() {
        return results.stream()
                .anyMatch(result -> !result.success());
    }

    public boolean hasSuccesses() {
        return results.stream()
                .anyMatch(ActionExecutionResult::success);
    }

    public long successCount() {
        return results.stream()
                .filter(ActionExecutionResult::success)
                .count();
    }

    public long failureCount() {
        return results.stream()
                .filter(result -> !result.success())
                .count();
    }
}
