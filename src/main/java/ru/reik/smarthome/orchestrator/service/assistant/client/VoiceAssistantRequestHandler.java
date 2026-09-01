package ru.reik.smarthome.orchestrator.service.assistant.client;

import org.springframework.stereotype.Component;
import ru.reik.smarthome.orchestrator.dto.assistant.AssistantClientType;
import ru.reik.smarthome.orchestrator.dto.assistant.AssistantHandlerResult;
import ru.reik.smarthome.orchestrator.dto.assistant.AssistantRequest;
import ru.reik.smarthome.orchestrator.service.voice.VoiceService;

@Component
public class VoiceAssistantRequestHandler implements AssistantRequestHandler {

    private final VoiceService voiceService;

    public VoiceAssistantRequestHandler(VoiceService voiceService) {
        this.voiceService = voiceService;
    }

    @Override
    public AssistantClientType clientType() {
        return AssistantClientType.AUDIO;
    }

    @Override
    public AssistantHandlerResult handle(AssistantRequest request) {
        return voiceService.handle(request);
    }
}
