package ru.reik.smarthome.orchestrator.service.llm;

import org.springframework.stereotype.Service;
import ru.reik.smarthome.orchestrator.client.LlmClient;
import ru.reik.smarthome.orchestrator.dto.assistant.AssistantResponse;
import ru.reik.smarthome.orchestrator.dto.llm.LlmEntityContext;
import ru.reik.smarthome.orchestrator.dto.llm.LlmPlan;
import ru.reik.smarthome.orchestrator.dto.smarthome.SmartHomeEntity;
import ru.reik.smarthome.orchestrator.service.CatalogService;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
public class LlmPlanningService {
    private final CatalogService catalogService;
    private final LlmClient llmClient;
    private final LlmPromptFactory promptFactory;
    private final LlmPlanMapper planMapper;
    private final ObjectMapper objectMapper;
    private final LlmContextService llmContextService;

    public LlmPlanningService(
            CatalogService catalogService,
            LlmClient llmClient,
            LlmPromptFactory promptFactory,
            LlmPlanMapper planMapper,
            ObjectMapper objectMapper, LlmContextService llmContextService
    ) {
        this.catalogService = catalogService;
        this.llmClient = llmClient;
        this.promptFactory = promptFactory;
        this.planMapper = planMapper;
        this.objectMapper = objectMapper;
        this.llmContextService = llmContextService;
    }

    public AssistantResponse createPlan(String command) {
        if (command == null || command.isBlank()) {
            throw new IllegalArgumentException(
                    "Команда для модели не должна быть пустой"
            );
        }

        //List<SmartHomeEntity> entities = catalogService.getEntities();
        List<LlmEntityContext> entities = llmContextService.getCurrentEntities();

        if (entities.isEmpty()) {
            throw new IllegalStateException(
                    "Каталог умного дома пуст. Сначала выполни /ha_refresh"
            );
        }

        String responseJson = llmClient.generateJson(
                promptFactory.buildSystemPrompt(),
                promptFactory.buildUserPrompt(
                        command,
                        entities
                )
        );

        LlmPlan plan = objectMapper.readValue(
                responseJson,
                LlmPlan.class
        );

        return planMapper.map(plan);
    }
}
