package ru.reik.smarthome.orchestrator.service.telegram;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.reik.smarthome.orchestrator.client.TelegramClient;
import ru.reik.smarthome.orchestrator.dto.assistant.AssistantClientType;
import ru.reik.smarthome.orchestrator.dto.assistant.AssistantRequest;
import ru.reik.smarthome.orchestrator.dto.assistant.AssistantResponse;
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

    @PostConstruct
    public void init() {
        telegramClient.dropUpdates(false);
    }

    @Scheduled(fixedDelayString = "${telegram.polling-delay-ms:1500}")
    public void poll() {
        List<Map<String, Object>> updates = getUpdatesWithRetry();

        for (Map<String, Object> update : updates) {
            processUpdate(update);
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
        if (from == null) {
            log.warn("Telegram message has no sender: {}", update);

            return;
        }

        String id = from.get("id").toString();
        if (!telegramAccessService.isOwner(id)) {
            sendMessageWithRetry(chatId.longValue(), "Ты ничего не перепутал?");

            return;
        }

        log.info("Telegram command from chat {}: {}", chatId, text);

        AssistantResponse assistantResponse = assistantOrchestratorService.handle(new AssistantRequest(
                AssistantClientType.TELEGRAM,
                chatId.toString(),
                text
        ));

        sendMessageWithRetry(chatId.longValue(), assistantResponse.answer());
    }

    private void processUpdate(
            Map<String, Object> update
    ) {
        Number updateId =
                getNumber(update, "update_id");

        if (updateId == null) {
            log.warn(
                    "Telegram update has no update_id: {}",
                    update
            );

            return;
        }

        try {
            handleUpdate(update);
        } catch (Exception exception) {
            log.error(
                    "Telegram update processing failed, "
                            + "updateId={}",
                    updateId,
                    exception
            );

            sendProcessingErrorSafely(update);
        } finally {
            /*
             * Даже ошибочное сообщение считаем обработанным.
             * Иначе оно навсегда заблокирует очередь.
             */
            offset = Math.max(
                    offset,
                    updateId.longValue() + 1
            );
        }
    }

    private List<Map<String, Object>> getUpdatesWithRetry() {
        int maxAttempts = 3;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                log.debug("Polling Telegram updates. Current offset: {}", offset);

                return telegramClient.getUpdates(offset);
            } catch (Exception exception) {
                if (attempt == maxAttempts) {
                    log.error("Telegram polling failed after {} attempts", maxAttempts, exception);

                    return List.of();
                }

                log.warn("Telegram polling attempt {}/{} failed", attempt, maxAttempts, exception);

                if (sleepBeforeRetry()) {
                    return List.of();
                }
            }
        }

        return List.of();
    }

    private boolean sleepBeforeRetry() {
        try {
            Thread.sleep(2000);

            return false;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            log.warn("Telegram retry sleep interrupted", exception);

            return true;
        }
    }

    private Number getNumber(
            Map<String, Object> source,
            String key
    ) {
        Object value = source.get(key);

        if (value instanceof Number number) {
            return number;
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private void sendProcessingErrorSafely(
            Map<String, Object> update
    ) {
        try {
            Map<String, Object> message =
                    (Map<String, Object>)
                            update.get("message");

            if (message == null) {
                return;
            }

            Map<String, Object> chat =
                    (Map<String, Object>)
                            message.get("chat");

            if (chat == null) {
                return;
            }

            Number chatId =
                    getNumber(chat, "id");

            if (chatId == null) {
                return;
            }

            sendMessageWithRetry(
                    chatId.longValue(),
                    "Не удалось обработать команду. "
                            + "Ошибка уже записана в журнал."
            );
        } catch (Exception exception) {
            log.error(
                    "Failed to send processing error "
                            + "to Telegram",
                    exception
            );
        }
    }

    private void sendMessageWithRetry(
            long chatId,
            String text
    ) {
        int maxAttempts = 3;

        for (
                int attempt = 1;
                attempt <= maxAttempts;
                attempt++
        ) {
            try {
                telegramClient.sendMessage(
                        chatId,
                        text
                );

                return;
            } catch (Exception exception) {
                if (attempt == maxAttempts) {
                    log.error(
                            "Failed to send Telegram message "
                                    + "after {} attempts, chatId={}",
                            maxAttempts,
                            chatId,
                            exception
                    );

                    return;
                }

                log.warn(
                        "Telegram send attempt {}/{} failed, "
                                + "chatId={}",
                        attempt,
                        maxAttempts,
                        chatId,
                        exception
                );

                if (sleepBeforeRetry()) {
                    return;
                }
            }
        }

    }
}
