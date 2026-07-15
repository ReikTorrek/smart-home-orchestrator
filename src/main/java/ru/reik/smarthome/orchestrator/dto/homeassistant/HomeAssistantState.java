package ru.reik.smarthome.orchestrator.dto.homeassistant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HomeAssistantState(
        @JsonProperty("entity_id")
        String entityId,
        String state,

        @JsonProperty("attributes")
        HomeAssistantStateAttributes attributes,

        @JsonProperty("last_changed")
        OffsetDateTime lastChanged,

        @JsonProperty("last_updated")
        OffsetDateTime lastUpdated,

        @JsonProperty("min_color_temp_kelvin")
        Integer minColorTempKelvin,

        @JsonProperty("max_color_temp_kelvin")
        Integer maxColorTempKelvin
) {
}
