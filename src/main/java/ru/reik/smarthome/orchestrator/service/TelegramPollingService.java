package ru.reik.smarthome.orchestrator.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.reik.smarthome.orchestrator.client.TelegramClient;
import ru.reik.smarthome.orchestrator.dto.AssistantResponse;

import java.util.List;
import java.util.Map;

@Service
public class TelegramPollingService {
    private static final Logger log = LoggerFactory.getLogger(TelegramPollingService.class);

    private final TelegramClient telegramClient;
    private final CommandService commandService;

    private long offset = 0;

    public TelegramPollingService(
            TelegramClient telegramClient,
            CommandService commandService
    ) {
        this.telegramClient = telegramClient;
        this.commandService = commandService;
    }

    @Scheduled(fixedDelayString = "${telegram.polling-delay-ms:1500}")
    public void poll() {
        try {
            log.debug("Polling Telegram updates. Current offset: {}", offset);

            List<Map<String, Object>> updates = telegramClient.getUpdates(offset);

            if (updates.isEmpty()) {
                return;
            }

            for (Map<String, Object> update : updates) {
                handleUpdate(update);

                Number updateId = (Number) update.get("update_id");
                offset = updateId.longValue() + 1;
            }
        } catch (Exception exception) {
            log.error("Telegram polling failed", exception);
        }
    }

    @SuppressWarnings("unchecked")
    private void handleUpdate(Map<String, Object> update) {
        Map<String, Object> message = (Map<String, Object>) update.get("message");

        if (message == null) {
            return;
        }

        String text = (String) message.get("text");
        Map<String, Object> chat = (Map<String, Object>) message.get("chat");

        if (text == null || text.isBlank() || chat == null) {
            return;
        }

        Number chatId = (Number) chat.get("id");
        Map<String, Object> from = (Map<String, Object>) message.get("from");
        String id = from.get("id").toString();
        if (!id.equals("451817956")) {
            telegramClient.sendMessage(chatId.longValue(), "Ты ничего не перепутал?");
        }

        log.info("Telegram command from chat {}: {}", chatId, text);

        AssistantResponse assistantResponse = commandService.handle(text);

        telegramClient.sendMessage(chatId.longValue(), assistantResponse.answer());
    }
}
