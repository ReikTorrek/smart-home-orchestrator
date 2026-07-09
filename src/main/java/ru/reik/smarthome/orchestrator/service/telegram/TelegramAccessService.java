package ru.reik.smarthome.orchestrator.service.telegram;

import org.springframework.stereotype.Service;
import ru.reik.smarthome.orchestrator.config.TelegramBotProperties;

import java.util.Objects;

@Service
public class TelegramAccessService {
    private final TelegramBotProperties telegramBotProperties;

    public TelegramAccessService(TelegramBotProperties telegramBotProperties) {
        this.telegramBotProperties = telegramBotProperties;
    }

    public boolean isOwner(String userId) {
        return Objects.equals(userId, telegramBotProperties.ownerId());
    }
}
