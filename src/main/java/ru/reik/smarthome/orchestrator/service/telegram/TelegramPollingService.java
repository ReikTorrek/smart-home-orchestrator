package ru.reik.smarthome.orchestrator.service.telegram;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.reik.smarthome.orchestrator.client.TelegramClient;
import ru.reik.smarthome.orchestrator.dto.assistant.AssistantResponse;
import ru.reik.smarthome.orchestrator.service.CommandService;
import ru.reik.smarthome.orchestrator.service.assistant.AssistantOrchestratorService;

import java.util.List;
import java.util.Map;

@Service
public class TelegramPollingService {
    private static final Logger log = LoggerFactory.getLogger(TelegramPollingService.class);

    private final TelegramClient telegramClient;
    private final AssistantOrchestratorService  assistantOrchestratorService;
    private final TelegramAccessService telegramAccessService;

    private long offset = 0;

    public TelegramPollingService(
            TelegramClient telegramClient,
            AssistantOrchestratorService assistantOrchestratorService,
            TelegramAccessService telegramAccessService
    ) {
        this.telegramClient = telegramClient;
        this.assistantOrchestratorService = assistantOrchestratorService;
        this.telegramAccessService = telegramAccessService;
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
        if (!telegramAccessService.isOwner(id)) {
            telegramClient.sendMessage(chatId.longValue(), "Ты ничего не перепутал?");

            return;
        }

        log.info("Telegram command from chat {}: {}", chatId, text);

        AssistantResponse assistantResponse = assistantOrchestratorService.handle(text);

        int maxAttempts = 3;
        for (int i = 0; i < maxAttempts; i++) {
            try {
                telegramClient.sendMessage(chatId.longValue(), assistantResponse.answer());
                break;
            } catch (Exception exception) {
                log.error("Telegram command failed", exception);
            }
        }
    }
}
