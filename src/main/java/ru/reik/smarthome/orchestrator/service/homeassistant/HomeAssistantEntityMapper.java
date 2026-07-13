package ru.reik.smarthome.orchestrator.service.homeassistant;

import org.springframework.stereotype.Service;
import ru.reik.smarthome.orchestrator.config.homeassistant.HomeAssistantDiscoveryProperties;
import ru.reik.smarthome.orchestrator.dto.homeassistant.HomeAssistantState;
import ru.reik.smarthome.orchestrator.dto.smarthome.ActionParameterDefinition;
import ru.reik.smarthome.orchestrator.dto.smarthome.SmartHomeAction;
import ru.reik.smarthome.orchestrator.dto.smarthome.SmartHomeEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class HomeAssistantEntityMapper {
    private final HomeAssistantDiscoveryProperties discoveryProperties;
    private final HomeAssistantCapabilityResolver capabilityResolver;

    public HomeAssistantEntityMapper(
            HomeAssistantDiscoveryProperties discoveryProperties,
            HomeAssistantCapabilityResolver capabilityResolver
    ) {
        this.discoveryProperties = discoveryProperties;
        this.capabilityResolver = capabilityResolver;
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
                .map(action -> mapAction(state, domain, action))
                .toList();
    }

    private boolean isActionAvailable(
            HomeAssistantState state,
            HomeAssistantDiscoveryProperties.Action action
    ) {
        if (action.requiredCapability() == null) {
            return true;
        }

        return capabilityResolver.supports(state, action.requiredCapability());
    }

    private SmartHomeAction mapAction(
            HomeAssistantState state,
            String domain,
            HomeAssistantDiscoveryProperties.Action action
    ) {
        return new SmartHomeAction(
                action.code(),
                action.title(),
                domain,
                action.service(),
                Map.of(),
                resolveParameters(state, action)
        );
    }

    private Map<String, ActionParameterDefinition> resolveParameters(
            HomeAssistantState state,
            HomeAssistantDiscoveryProperties.Action action
    ) {
        return action.parameters().entrySet().stream()
                .filter(entry -> isParameterAvailable(
                        state,
                        entry.getValue()
                ))
                .collect(Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        entry -> mapParameter(entry.getValue())
                ));
    }

    private boolean isParameterAvailable(
            HomeAssistantState state,
            HomeAssistantDiscoveryProperties.Parameter parameter
    ) {
        if (parameter.requiredCapability() == null) {
            return true;
        }

        return capabilityResolver.supports(
                state,
                parameter.requiredCapability()
        );
    }

    private ActionParameterDefinition mapParameter(
            HomeAssistantDiscoveryProperties.Parameter parameter
    ) {
        return new ActionParameterDefinition(
                parameter.type(),
                parameter.required(),
                parameter.minimum(),
                parameter.maximum(),
                parameter.description()
        );
    }
}
