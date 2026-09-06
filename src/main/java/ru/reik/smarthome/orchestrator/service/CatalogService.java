package ru.reik.smarthome.orchestrator.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import ru.reik.smarthome.orchestrator.client.HomeAssistantClient;
import ru.reik.smarthome.orchestrator.config.homeassistant.HomeAssistantDiscoveryProperties;
import ru.reik.smarthome.orchestrator.dto.homeassistant.HomeAssistantState;
import ru.reik.smarthome.orchestrator.dto.smarthome.CatalogRefreshResult;
import ru.reik.smarthome.orchestrator.dto.smarthome.SmartHomeEntity;
import ru.reik.smarthome.orchestrator.service.homeassistant.HomeAssistantEntityMapper;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class CatalogService {
    private static final Logger log = LoggerFactory.getLogger(CatalogService.class);

    private final HomeAssistantClient homeAssistantClient;
    private final HomeAssistantEntityMapper homeAssistantEntityMapper;
    private final HomeAssistantDiscoveryProperties discoveryProperties;

    private final AtomicReference<List<SmartHomeEntity>> cachedEntities = new AtomicReference<>(List.of());
    private volatile Instant lastUpdate;

    public CatalogService(
            HomeAssistantClient homeAssistantClient,
            HomeAssistantEntityMapper homeAssistantEntityMapper,
            HomeAssistantDiscoveryProperties discoveryProperties
    ) {
        this.homeAssistantClient = homeAssistantClient;
        this.homeAssistantEntityMapper = homeAssistantEntityMapper;
        this.discoveryProperties = discoveryProperties;
    }

    public List<SmartHomeEntity> getEntities() {
        return cachedEntities.get();
    }

    public CatalogRefreshResult refresh() {
        List<HomeAssistantState> states = homeAssistantClient.getStates();

        List<SmartHomeEntity> entities = states.stream()
                .map(homeAssistantEntityMapper::map)
                .flatMap(Optional::stream)
                .filter(entity -> discoveryProperties.rules().get(entity.domain()).hasActions()
                        != entity.actionless())
                .toList();

        cachedEntities.set(List.copyOf(entities));
        lastUpdate = Instant.now();

        return new CatalogRefreshResult(entities.size(), lastUpdate);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void refreshOnStartUp() {
        try {
            refresh();
        } catch (Exception e) {
            log.warn("Error while refreshing catalog.", e);
        }
    }
}
