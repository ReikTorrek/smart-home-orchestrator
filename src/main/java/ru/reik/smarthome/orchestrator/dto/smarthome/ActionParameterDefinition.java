package ru.reik.smarthome.orchestrator.dto.smarthome;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

public record ActionParameterDefinition(
        ActionParameterType type,
        boolean required,
        Double minimum,
        Double maximum,
        Integer size,
        Double itemMinimum,
        Double itemMaximum,
        String description,

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        List<String> allowedValues
) {
    public ActionParameterDefinition {
        allowedValues = allowedValues == null
                ? List.of()
                : List.copyOf(allowedValues);
    }
}
