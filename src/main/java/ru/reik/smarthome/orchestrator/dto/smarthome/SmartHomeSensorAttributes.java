package ru.reik.smarthome.orchestrator.dto.smarthome;

public record SmartHomeSensorAttributes(
        String stateClass,
        String unitOfMeasurement,
        String friendlyName,
        String deviceClass
) {
}
