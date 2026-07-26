package ru.reik.smarthome.orchestrator.service.assistant.client;

import ru.reik.smarthome.orchestrator.dto.assistant.AssistantClientType;
import ru.reik.smarthome.orchestrator.dto.assistant.AssistantHandlerResult;
import ru.reik.smarthome.orchestrator.dto.assistant.AssistantRequest;

public interface AssistantRequestHandler {
    AssistantClientType clientType();

    AssistantHandlerResult handle(
            AssistantRequest request
    );
}
