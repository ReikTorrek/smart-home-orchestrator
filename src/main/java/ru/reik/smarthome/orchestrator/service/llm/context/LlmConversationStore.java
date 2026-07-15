package ru.reik.smarthome.orchestrator.service.llm.context;

import ru.reik.smarthome.orchestrator.dto.llm.LlmConversationKey;
import ru.reik.smarthome.orchestrator.dto.llm.LlmConversationTurn;

import java.util.List;

public interface LlmConversationStore {
    List<LlmConversationTurn> get(LlmConversationKey conversationKey);
    void append(LlmConversationKey conversationKey, LlmConversationTurn conversationTurn);
    void clear(LlmConversationKey conversationKey);
}
