package ru.reik.smarthome.orchestrator.service.telegram;

import org.springframework.stereotype.Service;
import ru.reik.smarthome.orchestrator.dto.assistant.AssistantHandlerResult;
import ru.reik.smarthome.orchestrator.dto.assistant.AssistantRequest;
import ru.reik.smarthome.orchestrator.dto.assistant.AssistantResponse;
import ru.reik.smarthome.orchestrator.dto.homeassistant.HomeAssistantActionPayload;
import ru.reik.smarthome.orchestrator.dto.smarthome.CatalogRefreshResult;
import ru.reik.smarthome.orchestrator.dto.smarthome.SmartHomeEntity;
import ru.reik.smarthome.orchestrator.service.CatalogService;
import ru.reik.smarthome.orchestrator.service.llm.LlmPlanningService;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CommandService {

    private final CatalogService catalogService;
    private final ObjectMapper objectMapper;
    private final LlmPlanningService  llmPlanningService;

    public CommandService(
            CatalogService catalogService,
            ObjectMapper objectMapper, LlmPlanningService llmPlanningService
    ) {
        this.catalogService = catalogService;
        this.objectMapper = objectMapper;
        this.llmPlanningService = llmPlanningService;
    }

    public AssistantHandlerResult handle(AssistantRequest request) {
        String text = request.text();

        if (text == null || text.isBlank()) {
            AssistantResponse response = AssistantResponse.text("Пустая команда.");

            return AssistantHandlerResult.skip(response);
        }

        String command = normalizeCommand(text);

        return switch (command) {
            case "/ha_entities"        -> handleHomeAssistantEntities();
            case "/ha_do"              -> handleHomeAssistantDo(text);
            case "/ha_refresh"         -> handleHomeAssistantRefresh();
            case "/clear_conversation" -> clearConversation(request);
            default                    -> handleAiPlan(request);
        };
    }

    private AssistantHandlerResult clearConversation(AssistantRequest request) {
        llmPlanningService.clearConversation(request);

        return AssistantHandlerResult.skip(AssistantResponse.text("Разговор почищен"));
    }

    private AssistantHandlerResult handleAiPlan(AssistantRequest request) {
        String userCommand = request.text();

        if (userCommand.isBlank()) {
            return AssistantHandlerResult.skip(AssistantResponse.text("Использование: /ai_plan <команда>"));
        }

        return llmPlanningService.createPlan(new AssistantRequest(request.clientType(), request.conversationId(), userCommand, null));
    }

    private AssistantHandlerResult handleHomeAssistantEntities() {
        List<SmartHomeEntity> entities = catalogService.getEntities();

        if (entities.isEmpty()) {
            AssistantResponse response = AssistantResponse.text("Каталог Home Assistant пока пуст. Попробуй чуть позже, или обновите каталог вручную");

            return AssistantHandlerResult.skip(response);
        }

        AssistantResponse response = AssistantResponse.text(entities.stream()
                .map(SmartHomeEntity::formatEntity)
                .collect(Collectors.joining("\n\n")));

        return AssistantHandlerResult.skip(response);
    }

    private AssistantHandlerResult handleHomeAssistantDo(String command) {
        String[] parts = command.trim().split("\\s+", 4);

        if (parts.length < 3) {
            AssistantResponse response = AssistantResponse.text("""
                Использование:
                /ha_do <entity_id> <action_code> [parameters_json]

                Примеры:
                /ha_do light.zb_5 turn_on
                /ha_do light.zb_5 set_brightness {"brightness_pct":35}
                """);

            return AssistantHandlerResult.skip(response);
        }

        try {
            Map<String, Object> parameters = Map.of();

            if (parts.length == 4) {
                parameters = objectMapper.readValue(
                        parts[3],
                        new TypeReference<>() {
                        }
                );
            }

            HomeAssistantActionPayload payload = new HomeAssistantActionPayload(parts[1], parts[2], parameters);
            AssistantResponse response = AssistantResponse.withAction(
                    "Выполнено: %s → %s".formatted(payload.entityId(),payload.actionCode()),
                    payload
            );

            return AssistantHandlerResult.save(response);
        } catch (Exception exception) {
            return AssistantHandlerResult.skip(AssistantResponse.text(exception.getMessage()));
        }
    }

    private AssistantHandlerResult handleHomeAssistantRefresh() {
        CatalogRefreshResult refreshResult = catalogService.refresh();

        return AssistantHandlerResult.skip(AssistantResponse.text(refreshResult.formattedForTelegram()));
    }

    private String normalizeCommand(String text) {
        String command = text.trim();

        int spaceIndex = command.indexOf(' ');
        if (spaceIndex > 0) {
            command = command.substring(0, spaceIndex);
        }

        return command;
    }
}
