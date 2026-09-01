package ru.reik.smarthome.orchestrator.service.llm;

import org.springframework.stereotype.Service;
import ru.reik.smarthome.orchestrator.client.llm.LlmClient;
import ru.reik.smarthome.orchestrator.dto.assistant.AssistantHandlerResult;
import ru.reik.smarthome.orchestrator.dto.assistant.AssistantRequest;
import ru.reik.smarthome.orchestrator.dto.llm.*;
import ru.reik.smarthome.orchestrator.service.llm.context.InMemoryLlmConversationStore;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;

@Service
public class LlmPlanningService {

    private final LlmClient llmClient;
    private final LlmPromptFactory promptFactory;
    private final LlmPlanMapper planMapper;
    private final ObjectMapper objectMapper;
    private final LlmContextService llmContextService;
    private final LlmEnvironmentContextService environmentContextService;
    private final InMemoryLlmConversationStore  conversationStore;

    public LlmPlanningService(
            LlmClient llmClient,
            LlmPromptFactory promptFactory,
            LlmPlanMapper planMapper,
            ObjectMapper objectMapper,
            LlmContextService llmContextService,
            LlmEnvironmentContextService environmentContextService,
            InMemoryLlmConversationStore conversationStore
    ) {
        this.llmClient = llmClient;
        this.promptFactory = promptFactory;
        this.planMapper = planMapper;
        this.objectMapper = objectMapper;
        this.llmContextService = llmContextService;
        this.environmentContextService = environmentContextService;
        this.conversationStore = conversationStore;
    }

    public AssistantHandlerResult createPlan(AssistantRequest request) {
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

        LlmEnvironmentContext envContext = environmentContextService.getCurrent();

        List<LlmMessage> messages = buildMessages(conversationKey, command, entities, envContext);

        String responseJson = llmClient.chat(messages, false);

        LlmPlan plan = objectMapper.readValue(
                responseJson,
                LlmPlan.class
        );

        return AssistantHandlerResult.save(planMapper.map(plan));
    }

    public void clearConversation(AssistantRequest request) {
        LlmConversationKey conversationKey = new LlmConversationKey(
                request.clientType(),
                request.conversationId()
        );

        conversationStore.clear(conversationKey);
    }

    private List<LlmMessage> buildMessages(
            LlmConversationKey key,
            String command,
            List<LlmEntityContext> entities,
            LlmEnvironmentContext envContext
    ) {
        List<LlmMessage> messages = new ArrayList<>();

        messages.add(LlmMessage.system(promptFactory.buildSystemPrompt()));

        for (LlmConversationTurn turn : conversationStore.get(key)) {
            messages.add(LlmMessage.user(turn.userMessage()));
            messages.add(LlmMessage.assistant(buildAssistantHistoryContent(turn)));
            //messages.add(LlmMessage.assistant(turn.executionResult()));
        }

        messages.add(LlmMessage.user(promptFactory.buildUserPrompt(command, entities, envContext)));

        return List.copyOf(messages);
    }

    private String buildAssistantHistoryContent(
            LlmConversationTurn turn
    ) {
        try {
            ObjectNode historyNode =
                    objectMapper.createObjectNode();

            historyNode.set(
                    "plan",
                    objectMapper.readTree(
                            turn.assistantResponse()
                    )
            );

            if (
                    turn.executionResult() != null
                            && !turn.executionResult().isBlank()
            ) {
                historyNode.set(
                        "executionResult",
                        objectMapper.readTree(
                                turn.executionResult()
                        )
                );
            }

            return objectMapper.writeValueAsString(
                    historyNode
            );
        } catch (JacksonException exception) {
            throw new IllegalStateException(
                    "Не удалось сформировать историю для модели",
                    exception
            );
        }
    }
}
