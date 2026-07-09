package ru.reik.smarthome.orchestrator.service;

import org.springframework.stereotype.Service;
import ru.reik.smarthome.orchestrator.config.homeassistant.HomeAssistantDiscoveryProperties;
import ru.reik.smarthome.orchestrator.dto.homeassistant.HomeAssistantState;
import ru.reik.smarthome.orchestrator.dto.smarthome.SmartHomeAction;
import ru.reik.smarthome.orchestrator.dto.smarthome.SmartHomeEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class HomeAssistantEntityMapper {
    private final HomeAssistantDiscoveryProperties discoveryProperties;

    public HomeAssistantEntityMapper(HomeAssistantDiscoveryProperties discoveryProperties) {
        this.discoveryProperties = discoveryProperties;
    }

    public Optional<SmartHomeEntity> map(HomeAssistantState state) {
        String entityId = state.entityId();
        Optional<String> domainOptional = extractDomain(entityId);

        if (domainOptional.isEmpty()) {
            return Optional.empty();
        }

        String domain = domainOptional.get();
        String name = extractName(state);

        List<SmartHomeAction> actions = resolveActions(state, domain);

        return Optional.of(new SmartHomeEntity(
                entityId,
                domain,
                name,
                state.state(),
                actions
        ));
    }

    private Optional<String> extractDomain(String entityId) {
        if (entityId.contains("mqtt")) { // reik: Просто потому что не хочу видеть mqtt в доступных действиях
            return Optional.empty();
        }
        int dotIndex = entityId.indexOf('.');

        if (dotIndex <= 0) {
            return Optional.empty();
        }

        String domain = entityId.substring(0, dotIndex);
        HomeAssistantDiscoveryProperties.Rule rule = discoveryProperties.rules().get(domain);

        if (rule == null || !rule.enabled()) {
            return Optional.empty();
        }

        return Optional.of(domain);
    }

    private String extractName(HomeAssistantState state) {
        Object friendlyName = state.attributes().friendlyName();

        if (friendlyName instanceof String name && !name.isBlank()) {
            return name;
        }

        return state.entityId();
    }

    private List<SmartHomeAction> resolveActions(HomeAssistantState state, String domain) {
        HomeAssistantDiscoveryProperties.Rule rule = discoveryProperties.rules().get(domain);

        if (rule == null || !rule.enabled()) {
            return List.of();
        }

        return rule.actions().stream()
                .filter(action -> isActionAvailable(state, action))
                .map(action -> new SmartHomeAction(
                        action.code(),
                        action.title(),
                        domain,
                        action.service(),
                        Map.of()
                ))
                .toList();
    }

    private boolean isActionAvailable(
            HomeAssistantState state,
            HomeAssistantDiscoveryProperties.Action action
    ) {
        if (action.requiredSupportedColorMode() == null) {
            return true;
        }

        Object supportedColorModes = state.attributes().supportedColorModes();

        if (!(supportedColorModes instanceof List<?> modes)) {
            return false;
        }

        return modes.contains(action.requiredSupportedColorMode());
    }
}
