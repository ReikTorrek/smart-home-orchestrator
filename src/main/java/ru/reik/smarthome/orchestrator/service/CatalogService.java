package ru.reik.smarthome.orchestrator.service;

import org.springframework.stereotype.Service;
import ru.reik.smarthome.orchestrator.client.HomeAssistantClient;
import ru.reik.smarthome.orchestrator.dto.homeassistant.HomeAssistantState;
import ru.reik.smarthome.orchestrator.dto.smarthome.SmartHomeEntity;

import java.util.List;
import java.util.Optional;

@Service
public class CatalogService {
    private final HomeAssistantClient homeAssistantClient;
    private final HomeAssistantEntityMapper homeAssistantEntityMapper;

    public CatalogService(HomeAssistantClient homeAssistantClient,
                          HomeAssistantEntityMapper homeAssistantEntityMapper
    ) {
        this.homeAssistantClient = homeAssistantClient;
        this.homeAssistantEntityMapper = homeAssistantEntityMapper;
    }

    public List<SmartHomeEntity> getEntities() {
        List<HomeAssistantState> states = homeAssistantClient.getStates();

        List<SmartHomeEntity> result = new java.util.ArrayList<>(List.of());

        for (HomeAssistantState state : states) {
            Optional<SmartHomeEntity> entity = homeAssistantEntityMapper.map(state);
            if (entity.isEmpty()) {
                continue;
            }
            result.add(entity.get());
        }

        return result;
    }
}
