package ru.reik.smarthome.orchestrator.service.homeassistant;

import org.springframework.stereotype.Service;
import ru.reik.smarthome.orchestrator.client.HomeAssistantClient;
import ru.reik.smarthome.orchestrator.dto.smarthome.SmartHomeAction;
import ru.reik.smarthome.orchestrator.dto.smarthome.SmartHomeEntity;
import ru.reik.smarthome.orchestrator.service.CatalogService;

import java.util.Map;

@Service
public class HomeAssistantActionService {
    private final HomeAssistantClient homeAssistantClient;
    private final CatalogService catalogService;

    public HomeAssistantActionService(HomeAssistantClient homeAssistantClient, CatalogService catalogService) {
        this.homeAssistantClient = homeAssistantClient;
        this.catalogService = catalogService;
    }

    public String execute(String entityId, String actionCode) {
        SmartHomeEntity entity = catalogService.getEntities().stream()
                .filter(item -> item.entityId().equals(entityId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown entityId: " + entityId));

        SmartHomeAction action = entity.actions().stream()
                .filter(item -> item.code().equals(actionCode))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Действие '%s' недоступно для %s".formatted(actionCode, entityId)
                ));

        homeAssistantClient.callService(
                action.haDomain(),
                action.haService(),
                Map.of("entity_id", entity.entityId())
        );

        return "Выполнено: %s → %s".formatted(entity.name(), action.title());
    }
}
