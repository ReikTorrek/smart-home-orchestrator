package ru.reik.smarthome.orchestrator.service;

import org.springframework.stereotype.Service;
import ru.reik.smarthome.orchestrator.dto.assistant.AssistantResponse;
import ru.reik.smarthome.orchestrator.dto.smarthome.CatalogRefreshResult;
import ru.reik.smarthome.orchestrator.dto.smarthome.SmartHomeEntity;
import ru.reik.smarthome.orchestrator.service.homeassistant.HomeAssistantActionService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommandService {

    private final CatalogService catalogService;
    private final HomeAssistantActionService homeAssistantActionService;

    public CommandService(
            CatalogService catalogService,
            HomeAssistantActionService homeAssistantActionService
    ) {
        this.catalogService = catalogService;
        this.homeAssistantActionService = homeAssistantActionService;
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
            default -> AssistantResponse.text("Неизвестная команда.");
        };
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
        String[] parts = command.trim().split("\\s+");

        if (parts.length < 3) {
            return AssistantResponse.text("""
                Использование:
                /ha_do <entity_id> <action_code>
                
                Пример:
                /ha_do light.zb_5 turn_on
                """);
        }

        try {
            return AssistantResponse.text(homeAssistantActionService.execute(parts[1], parts[2]));
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
