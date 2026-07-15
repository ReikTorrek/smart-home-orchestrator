package ru.reik.smarthome.orchestrator.service.assistant.client;

import org.springframework.stereotype.Component;
import ru.reik.smarthome.orchestrator.dto.assistant.AssistantClientType;
import ru.reik.smarthome.orchestrator.dto.assistant.AssistantRequest;
import ru.reik.smarthome.orchestrator.dto.assistant.AssistantResponse;
import ru.reik.smarthome.orchestrator.service.telegram.CommandService;

@Component
public class TelegramAssistantRequestHandler implements AssistantRequestHandler {
    private final CommandService commandService;

    public TelegramAssistantRequestHandler(CommandService commandService) {
        this.commandService = commandService;
    }

    @Override
    public AssistantClientType clientType() {
        return AssistantClientType.TELEGRAM;
    }

    @Override
    public AssistantResponse handle(AssistantRequest request) {
        return commandService.handle(request);
    }
}
