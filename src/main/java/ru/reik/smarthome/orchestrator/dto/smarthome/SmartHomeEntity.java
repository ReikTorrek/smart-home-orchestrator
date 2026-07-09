package ru.reik.smarthome.orchestrator.dto.smarthome;

import java.util.List;

public record SmartHomeEntity(
        String entityId,
        String domain,
        String name,
        String state,
        List<SmartHomeAction> actions
) {
}
