package ru.reik.smarthome.orchestrator.dto.smarthome;

import java.time.Instant;

public record CatalogRefreshResult(
        int size,
        Instant refreshTime
) {
    public String formattedForTelegram() {
        return "Каталог обновлён. Найдено сущностей: %d".formatted(size);
    }
}
