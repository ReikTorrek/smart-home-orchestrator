package ru.reik.smarthome.orchestrator.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ru.reik.smarthome.orchestrator.client.HomeAssistantClient;
import ru.reik.smarthome.orchestrator.dto.assistant.AssistantAction;
import ru.reik.smarthome.orchestrator.dto.assistant.AssistantResponse;
import ru.reik.smarthome.orchestrator.dto.homeassistant.HomeAssistantState;
import ru.reik.smarthome.orchestrator.dto.smarthome.SmartHomeAction;
import ru.reik.smarthome.orchestrator.dto.smarthome.SmartHomeEntity;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CommandService {

    private final CatalogService catalogService;

    public CommandService(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    public AssistantResponse handle(String text) {
        String result = handleCommand(text);

        return new AssistantResponse(
                result,
                List.of()
        );
    }

    public String handleCommand(String text) {
        if (text == null || text.isBlank()) {
            return "Пустая команда.";
        }

        String command = normalizeCommand(text);

        return switch (command) {
            case "/ha_entities" -> handleHomeAssistantEntities();
            default -> "Неизвестная команда.";
        };
    }

    private String handleHomeAssistantEntities() {
        List<SmartHomeEntity> entities = catalogService.getEntities();

        if (entities.isEmpty()) {
            return "Каталог Home Assistant пока пуст. Попробуй чуть позже.";
        }

        return entities.stream()
                .map(this::formatEntity)
                .collect(Collectors.joining("\n\n"));
    }


    private String formatEntity(SmartHomeEntity entity) {
        String actions = entity.actions().stream()
                .map(SmartHomeAction::title)
                .distinct()
                .collect(Collectors.joining(", "));

        return """
                %s
                ID: %s
                Состояние: %s
                Действия: %s
                """.formatted(
                entity.name(),
                entity.entityId(),
                entity.state(),
                actions
        ).trim();
    }

    private String normalizeCommand(String text) {
        String command = text.trim();

        int spaceIndex = command.indexOf(' ');
        if (spaceIndex > 0) {
            command = command.substring(0, spaceIndex);
        }

        return command;
    }

    private boolean containsAny(String text, String... phrases) {
        for (String phrase : phrases) {
            if (text.contains(phrase)) {
                return true;
            }
        }
        return false;
    }
}
