package ru.reik.smarthome.orchestrator.service.homeassistant;

import org.springframework.stereotype.Service;
import ru.reik.smarthome.orchestrator.client.HomeAssistantClient;
import ru.reik.smarthome.orchestrator.dto.homeassistant.HomeAssistantActionPayload;
import ru.reik.smarthome.orchestrator.dto.smarthome.SmartHomeAction;
import ru.reik.smarthome.orchestrator.dto.smarthome.SmartHomeEntity;
import ru.reik.smarthome.orchestrator.service.CatalogService;

import java.util.HashMap;
import java.util.Map;

@Service
public class HomeAssistantActionService {
    private final HomeAssistantClient homeAssistantClient;
    private final CatalogService catalogService;

    public HomeAssistantActionService(HomeAssistantClient homeAssistantClient, CatalogService catalogService) {
        this.homeAssistantClient = homeAssistantClient;
        this.catalogService = catalogService;
    }

    public String execute(HomeAssistantActionPayload payload) {
        SmartHomeEntity entity = catalogService.getEntities().stream()
                .filter(item -> item.entityId().equals(payload.entityId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown entityId: " + payload.entityId()));

        SmartHomeAction action = entity.actions().stream()
                .filter(item -> item.code().equals(payload.actionCode()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Действие '%s' недоступно для %s".formatted(payload.actionCode(), payload.entityId())
                ));

        Map<String, Object> requestBody = new HashMap<>();

        if (action.defaultPayload() != null) {
            requestBody.putAll(action.defaultPayload());
        }

        requestBody.putAll(payload.parameters());

        // Устанавливаем последним, чтобы вызывающий код
        // не мог подменить целевую сущность через parameters.
        requestBody.put("entity_id", entity.entityId());

        homeAssistantClient.callService(
                action.haDomain(),
                action.haService(),
                requestBody
        );

        return "Выполнено: %s → %s".formatted(entity.name(), action.title());
    }
}
