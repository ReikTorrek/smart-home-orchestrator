package ru.reik.smarthome.orchestrator.service.llm;

import org.springframework.stereotype.Service;
import ru.reik.smarthome.orchestrator.client.llm.LlmClient;
import ru.reik.smarthome.orchestrator.dto.assistant.AssistantRequest;
import ru.reik.smarthome.orchestrator.dto.assistant.AssistantResponse;
import ru.reik.smarthome.orchestrator.dto.llm.*;
import ru.reik.smarthome.orchestrator.service.CatalogService;
import ru.reik.smarthome.orchestrator.service.llm.context.InMemoryLlmConversationStore;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Service
public class LlmPlanningService {
    private final LlmClient llmClient;
    private final LlmPromptFactory promptFactory;
    private final LlmPlanMapper planMapper;
    private final ObjectMapper objectMapper;
    private final LlmContextService llmContextService;
    private final InMemoryLlmConversationStore  conversationStore;

    public LlmPlanningService(
            LlmClient llmClient,
            LlmPromptFactory promptFactory,
            LlmPlanMapper planMapper,
            ObjectMapper objectMapper, LlmContextService llmContextService,
            InMemoryLlmConversationStore conversationStore
    ) {
        this.llmClient = llmClient;
        this.promptFactory = promptFactory;
        this.planMapper = planMapper;
        this.objectMapper = objectMapper;
        this.llmContextService = llmContextService;
        this.conversationStore = conversationStore;
    }

    public AssistantResponse createPlan(AssistantRequest request) {
        String command = request.text();
        if (command == null || command.isBlank()) {
            throw new IllegalArgumentException(
                    "Команда для модели не должна быть пустой"
            );
        }

        List<LlmEntityContext> entities = llmContextService.getCurrentEntities();

        if (entities.isEmpty()) {
            throw new IllegalStateException(
                    "Каталог умного дома пуст. Сначала выполни /ha_refresh"
            );
        }

        LlmConversationKey conversationKey = new LlmConversationKey(
                request.clientType(),
                request.conversationId()
        );

        List<LlmMessage> messages = buildMessages(conversationKey, command, entities);

        String responseJson = llmClient.generateJson(messages);

        LlmPlan plan = objectMapper.readValue(
                responseJson,
                LlmPlan.class
        );

        AssistantResponse response = planMapper.map(plan);

        conversationStore.append(conversationKey, new LlmConversationTurn(command, objectMapper.writeValueAsString(response)));

        return response;
    }

    public void clearConversation(AssistantRequest request) {
        LlmConversationKey conversationKey = new LlmConversationKey(
                request.clientType(),
                request.conversationId()
        );

        conversationStore.clear(conversationKey);
    }

    private List<LlmMessage> buildMessages(LlmConversationKey key, String command, List<LlmEntityContext> entities) {
        List<LlmMessage> messages = new ArrayList<>();
        messages.add(LlmMessage.system(promptFactory.buildSystemPrompt()));

        for (LlmConversationTurn turn : conversationStore.get(key)) {
            messages.add(LlmMessage.user(turn.userMessage()));
            messages.add(LlmMessage.assistant(turn.assistantResponse()));
        }

        messages.add(LlmMessage.user(promptFactory.buildUserPrompt(command, entities)));

        return List.copyOf(messages);
    }
}
