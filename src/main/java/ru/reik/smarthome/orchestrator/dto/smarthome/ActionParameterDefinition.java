package ru.reik.smarthome.orchestrator.dto.smarthome;

public record ActionParameterDefinition(
        ActionParameterType type,
        boolean required,
        Double minimum,
        Double maximum,
        String description
) {
}
