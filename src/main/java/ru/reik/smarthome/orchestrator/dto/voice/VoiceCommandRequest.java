package ru.reik.smarthome.orchestrator.dto.voice;

import jakarta.validation.constraints.NotBlank;

public record VoiceCommandRequest(
        @NotBlank String text,
        @NotBlank String clientId,
        @NotBlank String conversationId
) {
}
