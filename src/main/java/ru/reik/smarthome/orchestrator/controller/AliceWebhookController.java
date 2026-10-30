package ru.reik.smarthome.orchestrator.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.reik.smarthome.orchestrator.dto.alice.AliceRequestDto;
import ru.reik.smarthome.orchestrator.dto.alice.AliceResponseDto;
import ru.reik.smarthome.orchestrator.dto.assistant.AssistantClientType;
import ru.reik.smarthome.orchestrator.dto.assistant.AssistantRequest;
import ru.reik.smarthome.orchestrator.dto.assistant.AssistantResponse;
import ru.reik.smarthome.orchestrator.service.assistant.AssistantOrchestratorService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@RestController
@RequestMapping("/api/v1/alice-webhook")
public class AliceWebhookController {
    private static final Logger log = LoggerFactory.getLogger(AliceWebhookController.class);

    private final AssistantOrchestratorService assistantOrchestratorService;
    private final String webhookSecret;
    private final String skillId;

    public AliceWebhookController(
            AssistantOrchestratorService assistantOrchestratorService,
            @Value("${alice.webhook-secret}") String webhookSecret,
            @Value("${alice.skill-id}") String skillId
    ) {
        this.assistantOrchestratorService = assistantOrchestratorService;
        this.webhookSecret = webhookSecret;
        this.skillId = skillId;
    }

    @PostMapping("/{secret}")
    public AliceResponseDto handle(@PathVariable String secret, @Valid @RequestBody AliceRequestDto request) {
        if (!MessageDigest.isEqual(webhookSecret.getBytes(StandardCharsets.UTF_8), secret.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        if (!skillId.equals(request.session().skillId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        String command = request.request().command();

        if (command.isBlank()) {
            return AliceResponseDto.speak("Привет. Я Рин. Слушаю тебя.", false);
        }

        AssistantRequest assistantRequest = new AssistantRequest(
                AssistantClientType.AUDIO,
                "alice:" + request.session().sessionId(),
                command,
                "alice"
        );

        try {
            // shortcut: requests may outlive Alice's 4.5s timeout; add a deadline before regular device control.
            AssistantResponse response = assistantOrchestratorService.handle(assistantRequest);
            return AliceResponseDto.speak(response.answer(), false);
        } catch (RuntimeException exception) {
            log.error("Alice request processing failed", exception);
            return AliceResponseDto.speak("Не удалось подтвердить результат запроса.", false);
        }
    }
}
