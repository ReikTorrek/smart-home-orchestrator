package ru.reik.smarthome.orchestrator.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ru.reik.smarthome.orchestrator.dto.telegram.TelegramBotCommand;
import ru.reik.smarthome.orchestrator.dto.telegram.TelegramFileResponse;

import java.util.List;
import java.util.Map;

@Service
public class TelegramClient {
    private final RestClient restClient;
    private final RestClient fileClient;

    private final int max_resend_attempts = 3;

    public TelegramClient(
            @Qualifier("telegramRestClient") RestClient restClient,
            @Qualifier("telegramFileRestClient") RestClient fileClient) {
        this.restClient = restClient;
        this.fileClient = fileClient;
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getUpdates(long offset) {
        Map<String, Object> response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/getUpdates")
                        .queryParam("timeout", 20)
                        .queryParam("offset", offset)
                        .build())
                .retrieve()
                .body(Map.class);

        if (response == null || !Boolean.TRUE.equals(response.get("ok"))) {
            throw new IllegalStateException("Telegram getUpdates returned bad response: " + response);
        }

        Object result = response.get("result");

        if (!(result instanceof List<?>)) {
            return List.of();
        }

        return (List<Map<String, Object>>) result;
    }

    public TelegramFileResponse getFile(String fileId) {
        TelegramFileResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/getFile")
                        .queryParam("file_id", fileId)
                        .build())
                .retrieve()
                .body(TelegramFileResponse.class);

        if (response == null || !response.ok()) {
            throw new IllegalStateException("Telegram getUpdates returned bad response: " + response);
        }

        return response;
    }

    public byte[] downloadFile(String filePath) {
        byte[] audio = fileClient.get()
                .uri("/" + filePath)
                .retrieve()
                .body(byte[].class);

        if (audio == null || audio.length == 0) {
            throw new IllegalStateException(
                    "Telegram returned empty file"
            );
        }

        return audio;
    }

    public void sendMessage(long chatId, String text) {
        restClient.post()
                .uri("/sendMessage")
                .body(Map.of(
                        "chat_id", chatId,
                        "text", text
                ))
                .retrieve()
                .toBodilessEntity();
    }

    public void registerCommands(List<TelegramBotCommand> commands, int attempt) {
        try {
            restClient.post()
                    .uri("/setMyCommands")
                    .body(Map.of("commands", commands))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Throwable throwable) {
            if (attempt >= this.max_resend_attempts) {
                throw throwable;
            }

            attempt++;
            registerCommands(commands, attempt);
        }
    }

    public void dropUpdates(int attempt) {
        try {
            restClient.post()
                    .uri("/deleteWebhook")
                    .body(Map.of("drop_pending_updates", true))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Throwable throwable) {
            if (attempt >= this.max_resend_attempts) {
                throw throwable;
            }
            attempt ++;
            dropUpdates(attempt);
        }
    }
}
