package ru.reik.smarthome.orchestrator.service.assistant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.reik.smarthome.orchestrator.dto.assistant.*;
import ru.reik.smarthome.orchestrator.dto.llm.LlmConversationKey;
import ru.reik.smarthome.orchestrator.dto.llm.LlmConversationTurn;
import ru.reik.smarthome.orchestrator.dto.llm.LlmExecutionResult;
import ru.reik.smarthome.orchestrator.service.assistant.client.AssistantRequestHandler;
import ru.reik.smarthome.orchestrator.service.assistant.client.AssistantRequestHandlerRegistry;
import ru.reik.smarthome.orchestrator.service.llm.LlmExecutionResultMapper;
import ru.reik.smarthome.orchestrator.service.llm.context.InMemoryLlmConversationStore;
import ru.reik.smarthome.orchestrator.service.telegram.CommandService;
import ru.reik.smarthome.orchestrator.service.llm.LlmPlanFormatter;
import ru.reik.smarthome.orchestrator.service.llm.LlmPlanningService;
import tools.jackson.databind.ObjectMapper;

import java.util.stream.Collectors;

@Service
public class AssistantOrchestratorService {
    private static final Logger log = LoggerFactory.getLogger(AssistantOrchestratorService.class);

    private final AssistantActionExecutor assistantActionExecutor;
    private final AssistantRequestHandlerRegistry  assistantRequestHandlerRegistry;
    private final ObjectMapper objectMapper;
    private final LlmExecutionResultMapper  executionResultMapper;
    private final InMemoryLlmConversationStore  conversationStore;

    public AssistantOrchestratorService(
            AssistantActionExecutor assistantActionExecutor,
            AssistantRequestHandlerRegistry assistantRequestHandlerRegistry,
            ObjectMapper objectMapper,
            LlmExecutionResultMapper executionResultMapper,
            InMemoryLlmConversationStore conversationStore
    ) {
        this.assistantActionExecutor = assistantActionExecutor;
        this.assistantRequestHandlerRegistry = assistantRequestHandlerRegistry;
        this.objectMapper = objectMapper;
        this.executionResultMapper = executionResultMapper;
        this.conversationStore = conversationStore;
    }

    public AssistantResponse handle(AssistantRequest request) {
        AssistantRequestHandler handler = assistantRequestHandlerRegistry.get(request.clientType());
        AssistantHandlerResult result = handler.handle(request);

        try {
            ActionExecutionReport report = assistantActionExecutor.executeAll(result.response().actions());

            AssistantHandlerResult resultResponse = buildResultResponse(report, result);
            saveConversationTurn(request, resultResponse, report);

            return resultResponse.response();
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

    private void saveConversationTurn(
            AssistantRequest request,
            AssistantHandlerResult resultResponse,
            ActionExecutionReport report
    ) {
        if (resultResponse.policy() == ConversationPolicy.SKIP) {
            return;
        }

        LlmConversationKey llmConversationKey = new LlmConversationKey(request.clientType(), request.conversationId());
        String responseJson = objectMapper.writeValueAsString(resultResponse.response());

        LlmExecutionResult executionResult = executionResultMapper.map(report);
        String executionResultJson = objectMapper.writeValueAsString(executionResult);

        conversationStore.append(
                llmConversationKey,
                new LlmConversationTurn(
                        request.text(),
                        responseJson,
                        executionResultJson
                ))
        ;
    }

    private AssistantHandlerResult buildResultResponse(ActionExecutionReport report, AssistantHandlerResult result) {
        if (report.isSuccessful()) {
            return result;
        }

        if (!report.hasSuccesses()) {
            AssistantResponse response = AssistantResponse.text(buildFailureMessage(report, result.response()));

            return new AssistantHandlerResult(response, result.policy());
        }

        AssistantResponse response = AssistantResponse.text(buildPartialSuccessMessage(report, result.response()));

        return new AssistantHandlerResult(response, result.policy());
    }

    private String buildFailureMessage(ActionExecutionReport report, AssistantResponse response) {
        return """
                Ответ: %s
                Не удалось выполнить действия:
                %s
                """.formatted(response.answer(), formatFailures(report));
    }

    private String buildPartialSuccessMessage(ActionExecutionReport report, AssistantResponse response) {
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

    private String formatFailures(ActionExecutionReport report) {
        return report.results().stream()
                .filter(result -> !result.success())
                .map(this::formatFailure)
                .collect(Collectors.joining("\n"));
    }

    private String formatFailure(ActionExecutionResult result) {
        return "- %s → %s: %s".formatted(
                result.payload().entityId(),
                result.payload().actionCode(),
                result.error()
        );
    }
}
