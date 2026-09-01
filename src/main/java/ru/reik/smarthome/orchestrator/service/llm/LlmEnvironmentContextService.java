package ru.reik.smarthome.orchestrator.service.llm;

import org.springframework.stereotype.Service;
import ru.reik.smarthome.orchestrator.config.assistant.AssistantContextProperties;
import ru.reik.smarthome.orchestrator.dto.llm.LlmEnvironmentContext;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class LlmEnvironmentContextService {
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ISO_LOCAL_DATE;

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm");

    private final ZoneId zoneId;

    public LlmEnvironmentContextService(
            AssistantContextProperties properties
    ) {
        this.zoneId = ZoneId.of(
                properties.timeZone()
        );
    }

    public LlmEnvironmentContext getCurrent() {
        ZonedDateTime now =
                ZonedDateTime.now(zoneId);

        return new LlmEnvironmentContext(
                now.format(DATE_FORMAT),
                now.format(TIME_FORMAT),
                now.getDayOfWeek().name(),
                zoneId.getId()
        );
    }
}
