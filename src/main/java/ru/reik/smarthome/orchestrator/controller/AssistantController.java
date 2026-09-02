package ru.reik.smarthome.orchestrator.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.reik.smarthome.orchestrator.dto.assistant.AssistantClientType;
import ru.reik.smarthome.orchestrator.dto.assistant.AssistantRequest;
import ru.reik.smarthome.orchestrator.dto.assistant.AssistantResponse;
import ru.reik.smarthome.orchestrator.dto.voice.VoiceCommandRequest;
import ru.reik.smarthome.orchestrator.dto.voice.VoiceCommandResponse;
import ru.reik.smarthome.orchestrator.service.assistant.AssistantOrchestratorService;

@RestController
@RequestMapping("/api/v1/assistant")
public class AssistantController {
    private final AssistantOrchestratorService assistantOrchestratorService;

    public AssistantController(AssistantOrchestratorService assistantOrchestratorService) {
        this.assistantOrchestratorService = assistantOrchestratorService;
    }

    @PostMapping
    public VoiceCommandResponse handle(@Valid @RequestBody VoiceCommandRequest request) {
        AssistantRequest assistantRequest = new AssistantRequest(
                AssistantClientType.AUDIO,
                request.conversationId(),
                request.text(),
                null
        );

        AssistantResponse response = assistantOrchestratorService.handle(assistantRequest);

        return new VoiceCommandResponse(response.answer());
    }
}
