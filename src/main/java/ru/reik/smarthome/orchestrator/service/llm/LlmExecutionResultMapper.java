package ru.reik.smarthome.orchestrator.service.llm;

import org.springframework.stereotype.Component;
import ru.reik.smarthome.orchestrator.dto.assistant.ActionExecutionReport;
import ru.reik.smarthome.orchestrator.dto.llm.LlmExecutionResult;

import java.util.List;

@Component
public class LlmExecutionResultMapper {
    public LlmExecutionResult map(ActionExecutionReport actionExecutionReport) {
        List<LlmExecutionResult.ActionResult> actions = actionExecutionReport.results().stream()
                .map(result -> new LlmExecutionResult.ActionResult(
                        result.payload().entityId(),
                        result.payload().actionCode(),
                        result.success(),
                        result.error()
                ))
                .toList();

        return new LlmExecutionResult(
                resolveStatus(actionExecutionReport),
                actionExecutionReport.successCount(),
                actionExecutionReport.failureCount(),
                actions
        );
    }

    private String resolveStatus(
            ActionExecutionReport report
    ) {
        if (report.results().isEmpty()) {
            return "NO_ACTIONS";
        }

        if (report.isSuccessful()) {
            return "SUCCESS";
        }

        if (report.hasSuccesses()) {
            return "PARTIAL_SUCCESS";
        }

        return "FAILURE";
    }
}
