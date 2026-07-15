package ru.reik.smarthome.orchestrator.dto.homeassistant;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record HomeAssistantStateAttributes(
        @JsonProperty("friendly_name")
        String friendlyName,

        @JsonProperty("effect_list")
        List<String> effectList,

        @JsonProperty("supported_color_modes")
        List<String> supportedColorModes,

        @JsonProperty("supported_features")
        Integer supportedFeatures,

        Integer brightness,

        @JsonProperty("color_mode")
        String colorMode,

        @JsonProperty("color_temp_kelvin")
        Integer colorTempKelvin,

        @JsonProperty("rgb_color")
        List<Integer> rgbColor,

        @JsonProperty("xy_color")
        List<Double> xyColor,

        @JsonProperty("hs_color")
        List<Double> hsColor,

        @JsonProperty("min_color_temp_kelvin")
        Integer minColorTempKelvin,

        @JsonProperty("max_color_temp_kelvin")
        Integer maxColorTempKelvin,

        String effect
) {
        public HomeAssistantStateAttributes {
                effectList = effectList == null ? List.of() : effectList;
        }
}
