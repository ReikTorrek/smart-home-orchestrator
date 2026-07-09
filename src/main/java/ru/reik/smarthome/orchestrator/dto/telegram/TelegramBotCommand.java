package ru.reik.smarthome.orchestrator.dto.telegram;

public record TelegramBotCommand(
        String command,
        String description
) {
}
