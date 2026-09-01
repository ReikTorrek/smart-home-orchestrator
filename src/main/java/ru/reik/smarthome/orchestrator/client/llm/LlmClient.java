package ru.reik.smarthome.orchestrator.client.llm;

import ru.reik.smarthome.orchestrator.dto.llm.LlmMessage;

import java.util.List;

public interface LlmClient {
    /**
     * Возвращает JSON-текст, созданный моделью.
     */
    String chat(List<LlmMessage> messages, boolean isRetry);
}
