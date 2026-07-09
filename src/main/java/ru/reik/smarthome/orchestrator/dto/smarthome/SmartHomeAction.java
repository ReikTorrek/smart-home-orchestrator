package ru.reik.smarthome.orchestrator.dto.smarthome;

import java.util.Map;

public record SmartHomeAction(
        String code,
        String title,
        String haDomain,
        String haService,
        Map<String, Object> defaultPayload
) {
}
