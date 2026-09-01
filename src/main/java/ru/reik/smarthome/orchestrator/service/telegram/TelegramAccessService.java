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

    public boolean isOwner(String userId, String chatId) {
        return isOwnedChatId(chatId) || isOwnedUser(userId);
    }

    public boolean isOwnedUser(String userId) {
        return Objects.equals(userId, telegramBotProperties.ownerId());
    }

    public boolean isOwnedChatId(String chatId) {
        return Objects.equals(chatId, telegramBotProperties.ownerChatId());
    }
}
