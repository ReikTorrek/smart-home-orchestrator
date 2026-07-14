package ru.reik.smarthome.orchestrator.service.assistant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.reik.smarthome.orchestrator.dto.assistant.ActionExecutionReport;
import ru.reik.smarthome.orchestrator.dto.assistant.ActionExecutionResult;
import ru.reik.smarthome.orchestrator.dto.assistant.AssistantRequest;
import ru.reik.smarthome.orchestrator.dto.assistant.AssistantResponse;
import ru.reik.smarthome.orchestrator.service.assistant.client.AssistantRequestHandler;
import ru.reik.smarthome.orchestrator.service.assistant.client.AssistantRequestHandlerRegistry;
import ru.reik.smarthome.orchestrator.service.telegram.CommandService;
import ru.reik.smarthome.orchestrator.service.llm.LlmPlanFormatter;
import ru.reik.smarthome.orchestrator.service.llm.LlmPlanningService;

import java.util.stream.Collectors;

@Service
public class AssistantOrchestratorService {
    private static final Logger log = LoggerFactory.getLogger(AssistantOrchestratorService.class);

    private final AssistantActionExecutor assistantActionExecutor;
    private final AssistantRequestHandlerRegistry  assistantRequestHandlerRegistry;

    public AssistantOrchestratorService(
            AssistantActionExecutor assistantActionExecutor,
            AssistantRequestHandlerRegistry assistantRequestHandlerRegistry
    ) {
        this.assistantActionExecutor = assistantActionExecutor;
        this.assistantRequestHandlerRegistry = assistantRequestHandlerRegistry;
    }

    public AssistantResponse handle(AssistantRequest request) {
        AssistantRequestHandler handler = assistantRequestHandlerRegistry.get(request.clientType());
        AssistantResponse response = handler.handle(request);

        try {
            ActionExecutionReport report = assistantActionExecutor.executeAll(response.actions());

            if (report.isSuccessful()) {
                return response;
            }

            if (!report.hasSuccesses()) {
                return AssistantResponse.text(
                        buildFailureMessage(report)
                );
            }

            return AssistantResponse.text(
                    buildPartialSuccessMessage(
                            response,
                            report
                    )
            );
        } catch (IllegalArgumentException exception) {
            return AssistantResponse.text(
                    exception.getMessage()
            );
        } catch (Exception exception) {
            log.error(
                    "Assistant action execution failed",
                    exception
            );

            return AssistantResponse.text(
                    "Не удалось выполнить действие в Home Assistant."
            );
        }
    }

    private String buildFailureMessage(
            ActionExecutionReport report
    ) {
        return """
                Не удалось выполнить действия:
                %s
                """.formatted(formatFailures(report));
    }

    private String buildPartialSuccessMessage(
            AssistantResponse response,
            ActionExecutionReport report
    ) {
        return """
                %s

                Выполнено действий: %d.
                Не удалось выполнить: %d.

                Ошибки:
                %s
                """.formatted(
                response.answer(),
                report.successCount(),
                report.failureCount(),
                formatFailures(report)
        );
    }

    private String formatFailures(
            ActionExecutionReport report
    ) {
        return report.results().stream()
                .filter(result -> !result.success())
                .map(this::formatFailure)
                .collect(Collectors.joining("\n"));
    }

    private String formatFailure(
            ActionExecutionResult result
    ) {
        return "- %s → %s: %s".formatted(
                result.payload().entityId(),
                result.payload().actionCode(),
                result.error()
        );
    }
}
