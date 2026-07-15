package ru.reik.smarthome.orchestrator.service.llm;

import org.springframework.stereotype.Service;
import ru.reik.smarthome.orchestrator.client.HomeAssistantClient;
import ru.reik.smarthome.orchestrator.dto.homeassistant.HomeAssistantState;
import ru.reik.smarthome.orchestrator.dto.homeassistant.HomeAssistantStateAttributes;
import ru.reik.smarthome.orchestrator.dto.llm.LlmEntityContext;
import ru.reik.smarthome.orchestrator.dto.smarthome.SmartHomeEntity;
import ru.reik.smarthome.orchestrator.service.CatalogService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LlmContextService {
    private final CatalogService catalogService;
    private final HomeAssistantClient homeAssistantClient;

    public LlmContextService(CatalogService catalogService, HomeAssistantClient homeAssistantClient) {
        this.catalogService = catalogService;
        this.homeAssistantClient = homeAssistantClient;
    }

    public List<LlmEntityContext> getCurrentEntities() {
        List<SmartHomeEntity> catalog = catalogService.getEntities();

        Map<String, HomeAssistantState> currentStates =
                homeAssistantClient.getStates()
                        .stream()
                        .collect(Collectors.toMap(
                                HomeAssistantState::entityId,
                                Function.identity()
                        ));

        return catalog.stream()
                .map(entity -> mapEntity(entity, currentStates.get(entity.entityId())))
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<LlmEntityContext> mapEntity(
            SmartHomeEntity entity,
            HomeAssistantState currentState
    ) {
        if (currentState == null) {
            return Optional.empty();
        }

        return Optional.of(new LlmEntityContext(
                entity.entityId(),
                entity.domain(),
                entity.name(),
                currentState.state(),
                extractCurrentValues(
                        entity.domain(),
                        currentState.attributes()
                ),
                entity.actions()
        ));
    }

    private Map<String, Object> extractCurrentValues(
            String domain,
            HomeAssistantStateAttributes attributes
    ) {
        if (attributes == null) {
            return Map.of();
        }

        Map<String, Object> values =
                new LinkedHashMap<>();

        if ("light".equals(domain)) {
            addLightValues(values, attributes);
        }

        return Map.copyOf(values);
    }

    private void addLightValues(
            Map<String, Object> values,
            HomeAssistantStateAttributes attributes
    ) {
        if (attributes.brightness() != null) {
            values.put(
                    "brightness_pct",
                    convertBrightnessToPercent(
                            attributes.brightness()
                    )
            );
        }

        if (attributes.colorMode() != null) {
            values.put(
                    "color_mode",
                    attributes.colorMode()
            );
        }

        if (attributes.colorTempKelvin() != null) {
            values.put(
                    "color_temp_kelvin",
                    attributes.colorTempKelvin()
            );
        }

        if (attributes.minColorTempKelvin() != null) {
            values.put(
                    "min_color_temp_kelvin",
                    attributes.minColorTempKelvin()
            );
        }

        if (attributes.maxColorTempKelvin() != null) {
            values.put(
                    "max_color_temp_kelvin",
                    attributes.maxColorTempKelvin()
            );
        }

        if (attributes.rgbColor() != null) {
            values.put(
                    "rgb_color",
                    attributes.rgbColor()
            );
        }

        if (attributes.xyColor() != null) {
            values.put(
                    "xy_color",
                    attributes.xyColor()
            );
        }

        if (attributes.hsColor() != null) {
            values.put(
                    "hs_color",
                    attributes.hsColor()
            );
        }

        if (attributes.effect() != null) {
            values.put(
                    "effect",
                    attributes.effect()
            );
        }
    }

    private int convertBrightnessToPercent(
            int brightness
    ) {
        return (int) Math.round(
                brightness * 100.0 / 255.0
        );
    }
}
