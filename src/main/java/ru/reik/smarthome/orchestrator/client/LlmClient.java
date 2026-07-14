package ru.reik.smarthome.orchestrator.client;

public interface LlmClient {
    /**
     * Возвращает JSON-текст, созданный моделью.
     */
    String generateJson(
            String systemPrompt,
            String userPrompt
    );
}
