package ru.reik.smarthome.orchestrator.config.homeassistant;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

@Validated
@ConfigurationProperties(prefix = "home-assistant-discovery")
public record HomeAssistantDiscoveryProperties(
        @NotEmpty Map<String, @Valid Rule> rules
) {
    public record Rule(
            boolean enabled,

            List<Action> actions
    ) {}

    public record Action(
            @NotBlank String code,
            @NotBlank String title,
            @NotBlank String service,

            String requiredSupportedColorMode
    ) {}
}
