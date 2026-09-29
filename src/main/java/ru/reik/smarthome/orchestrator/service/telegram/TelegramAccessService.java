package ru.reik.smarthome.orchestrator.service.telegram;

import org.springframework.stereotype.Service;
import ru.reik.smarthome.orchestrator.config.TelegramBotProperties;

import java.util.Map;
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

    public boolean isValidUpdate(Map<String, Object> update) {
        Map<String, Object> message = (Map<String, Object>)update.get("message");

        if (message == null) {
            return false;
        }

        Map<String, Object> chat = (Map<String, Object>)message.get("chat");

        if (chat == null) {
            return false;
        }

        Number chatId = (Number) chat.get("id");

        return chatId != null;
    }
}
