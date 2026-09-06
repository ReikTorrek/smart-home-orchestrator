package ru.reik.smarthome.orchestrator.dto.smarthome;

import java.util.List;
import java.util.stream.Collectors;

public record SmartHomeEntity(
        String entityId,
        String domain,
        String name,
        String state,
        List<SmartHomeAction> actions,
        List<SmartHomeSensorAttributes> attributes,
        boolean actionless
) {
    public String formatEntity() {
        String actions = this.actions().stream()
                .map(SmartHomeAction::title)
                .distinct()
                .collect(Collectors.joining(", "));

        return """
                %s
                ID: %s
                Состояние: %s
                Действия: %s
                """.formatted(
                this.name(),
                this.entityId(),
                this.state(),
                actions
        ).trim();
    }
}
