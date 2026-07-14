package ru.reik.smarthome.orchestrator.config.homeassistant;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import ru.reik.smarthome.orchestrator.dto.smarthome.ActionParameterType;
import ru.reik.smarthome.orchestrator.dto.smarthome.SmartHomeCapability;

import java.lang.reflect.Parameter;
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
            @NotBlank
            String code,

            @NotBlank
            String title,

            @NotBlank
            String service,

            SmartHomeCapability requiredCapability,

            @Valid
            Map<String, Parameter> parameters
    ) {
        public Action {
            parameters = parameters == null
                    ? Map.of()
                    : Map.copyOf(parameters);
        }
    }

    public record Parameter(
            @NotNull
            ActionParameterType type,
            boolean required,
            Double minimum,
            Double maximum,
            Integer size,
            Double itemMinimum,
            Double itemMaximum,
            @NotBlank
            String description,
            SmartHomeCapability requiredCapability
    ) {
        public Parameter {
            if (
                    minimum != null
                            && maximum != null
                            && minimum > maximum
            ) {
                throw new IllegalArgumentException(
                        "Parameter minimum must not be greater than maximum"
                );
            }
        }
    }
}