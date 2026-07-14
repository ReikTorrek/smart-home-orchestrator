package ru.reik.smarthome.orchestrator.service.telegram;

import org.springframework.stereotype.Service;
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

    public AssistantResponse handle(String text) {
        if (text == null || text.isBlank()) {
            return AssistantResponse.text("Пустая команда.");
        }

        String command = normalizeCommand(text);

        return switch (command) {
            case "/ha_entities" -> handleHomeAssistantEntities();
            case "/ha_do" -> handleHomeAssistantDo(text);
            case "/ha_refresh" -> handleHomeAssistantRefresh();
            case "/ai_plan" -> handleAiPlan(text);
            default -> AssistantResponse.text("Неизвестная команда.");
        };
    }

    private AssistantResponse handleAiPlan(String text) {
        String userCommand = text.substring("/ai_plan".length());

        if (userCommand.isBlank()) {
            return AssistantResponse.text(
                    "Использование: /ai_plan <команда>"
            );
        }

        return llmPlanningService.createPlan(userCommand);
    }

    private AssistantResponse handleHomeAssistantEntities() {
        List<SmartHomeEntity> entities = catalogService.getEntities();

        if (entities.isEmpty()) {
            return AssistantResponse.text("Каталог Home Assistant пока пуст. Попробуй чуть позже, или обновите каталог вручную");
        }

        return AssistantResponse.text(entities.stream()
                .map(SmartHomeEntity::formatEntity)
                .collect(Collectors.joining("\n\n")));
    }

    private AssistantResponse handleHomeAssistantDo(String command) {
        String[] parts = command.trim().split("\\s+", 4);

        if (parts.length < 3) {
            return AssistantResponse.text("""
                Использование:
                /ha_do <entity_id> <action_code> [parameters_json]

                Примеры:
                /ha_do light.zb_5 turn_on
                /ha_do light.zb_5 set_brightness {"brightness_pct":35}
                """);
        }

        try {
            Map<String, Object> parameters = Map.of();

            if (parts.length == 4) {
                parameters = objectMapper.readValue(
                        parts[3],
                        new TypeReference<Map<String, Object>>() {
                        }
                );
            }

            HomeAssistantActionPayload payload = new HomeAssistantActionPayload(parts[1], parts[2], parameters);
            return AssistantResponse.withAction(
                    "Выполнено: %s → %s".formatted(payload.entityId(),payload.actionCode()),
                    payload
            );
        } catch (Exception exception) {
            return AssistantResponse.text(exception.getMessage());
        }
    }

    private AssistantResponse handleHomeAssistantRefresh() {
        CatalogRefreshResult refreshResult = catalogService.refresh();

        return AssistantResponse.text(refreshResult.formattedForTelegram());
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
