package ru.reik.smarthome.orchestrator.service.homeassistant;

import org.springframework.stereotype.Service;
import ru.reik.smarthome.orchestrator.dto.homeassistant.HomeAssistantState;
import ru.reik.smarthome.orchestrator.dto.homeassistant.HomeAssistantStateAttributes;
import ru.reik.smarthome.orchestrator.dto.smarthome.SmartHomeCapability;

import java.util.List;
import java.util.Set;

@Service
public class HomeAssistantCapabilityResolver {

    private static final Set<String> BRIGHTNESS_COLOR_MODES =
            Set.of(
                    "brightness",
                    "color_temp",
                    "hs",
                    "xy",
                    "rgb",
                    "rgbw",
                    "rgbww",
                    "white"
            );

    private static final int TRANSITION_FEATURE = 32;

    public boolean supports(
            HomeAssistantState state,
            SmartHomeCapability capability
    ) {
        HomeAssistantStateAttributes attributes =
                state.attributes();

        if (attributes == null) {
            return false;
        }

        return switch (capability) {
            case BRIGHTNESS ->
                    supportsBrightness(attributes);

            case TRANSITION ->
                    supportsTransition(attributes);
        };
    }

    private boolean supportsBrightness(
            HomeAssistantStateAttributes attributes
    ) {
        List<String> modes = attributes.supportedColorModes();

        if (modes == null || modes.isEmpty()) {
            return false;
        }

        return modes.stream()
                .anyMatch(BRIGHTNESS_COLOR_MODES::contains);
    }

    private boolean supportsTransition(
            HomeAssistantStateAttributes attributes
    ) {
        Integer supportedFeatures =
                attributes.supportedFeatures();

        return supportedFeatures != null
                && (
                supportedFeatures
                        & TRANSITION_FEATURE
        ) != 0;
    }
}