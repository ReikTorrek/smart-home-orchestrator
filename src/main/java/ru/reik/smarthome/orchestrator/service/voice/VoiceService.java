package ru.reik.smarthome.orchestrator.service.voice;

import org.springframework.stereotype.Service;
import ru.reik.smarthome.orchestrator.dto.assistant.AssistantHandlerResult;
import ru.reik.smarthome.orchestrator.dto.assistant.AssistantRequest;
import ru.reik.smarthome.orchestrator.dto.assistant.AssistantResponse;
import ru.reik.smarthome.orchestrator.service.llm.LlmPlanningService;

@Service
public class VoiceService {
    private final LlmPlanningService llmPlanningService;

    public VoiceService(LlmPlanningService llmPlanningService) {
        this.llmPlanningService = llmPlanningService;
    }

    public AssistantHandlerResult handle(AssistantRequest request) {
        return handleAiPlan(request);
    }

    private AssistantHandlerResult handleAiPlan(AssistantRequest request) {
        String userCommand = request.text();

        if (userCommand.isBlank()) {
            return AssistantHandlerResult.skip(AssistantResponse.text("Текст команды не был распознан"));
        }

        return llmPlanningService.createPlan(new AssistantRequest(request.clientType(), request.conversationId(), userCommand, null));
    }
}
