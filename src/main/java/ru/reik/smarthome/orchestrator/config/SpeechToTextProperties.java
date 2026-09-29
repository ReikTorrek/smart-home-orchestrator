package ru.reik.smarthome.orchestrator.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "speech-to-text")
public record SpeechToTextProperties(
        @NotBlank String baseUrl,
        @NotBlank String apiKey
) {}
