package ru.reik.smarthome.orchestrator.dto.homeassistant;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record HomeAssistantStateAttributes(
        @JsonProperty("friendly_name")
        String friendlyName,
        @JsonProperty("effect_list")
        List<String> effectList,
        @JsonProperty("supported_color_modes")
        List<String> supportedColorModes
) {
}
