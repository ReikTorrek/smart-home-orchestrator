package ru.reik.smarthome.orchestrator.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ru.reik.smarthome.orchestrator.dto.telegram.TelegramBotCommand;

import java.util.List;
import java.util.Map;

@Service
public class TelegramClient {
    private final RestClient restClient;

    public TelegramClient(@Qualifier("telegramRestClient") RestClient restClient) {
        this.restClient = restClient;
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

    public void registerCommands(List<TelegramBotCommand> commands) {
        restClient.post()
                .uri("/setMyCommands")
                .body(Map.of("commands", commands))
                .retrieve()
                .toBodilessEntity();
    }
}
