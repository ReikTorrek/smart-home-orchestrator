package ru.reik.smarthome.orchestrator.service.assistant.client;

import ru.reik.smarthome.orchestrator.dto.assistant.AssistantClientType;
import ru.reik.smarthome.orchestrator.dto.assistant.AssistantRequest;
import ru.reik.smarthome.orchestrator.dto.assistant.AssistantResponse;

public interface AssistantRequestHandler {
    AssistantClientType clientType();

    AssistantResponse handle(
            AssistantRequest request
    );
}
