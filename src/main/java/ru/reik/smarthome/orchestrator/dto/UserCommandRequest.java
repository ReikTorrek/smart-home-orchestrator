package ru.reik.smarthome.orchestrator.dto;

import jakarta.validation.constraints.NotBlank;

public record UserCommandRequest(
        @NotBlank(message = "Text required")
        String text
) {
}
