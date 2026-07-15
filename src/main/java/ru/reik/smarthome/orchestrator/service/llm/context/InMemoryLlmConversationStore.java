package ru.reik.smarthome.orchestrator.service.llm.context;

import org.springframework.stereotype.Component;
import ru.reik.smarthome.orchestrator.dto.llm.LlmConversationKey;
import ru.reik.smarthome.orchestrator.dto.llm.LlmConversationTurn;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryLlmConversationStore implements LlmConversationStore {

    private static final int MAX_TURNS = 6;

    private final Map<LlmConversationKey, Deque<LlmConversationTurn>> conversations = new ConcurrentHashMap<>();

    @Override
    public List<LlmConversationTurn> get(LlmConversationKey conversationKey) {
        Deque<LlmConversationTurn> conversationTurns = conversations.get(conversationKey);

        if (conversationTurns == null) {
            return List.of();
        }

        synchronized (conversationTurns) {
            return List.copyOf(conversationTurns);
        }
    }

    @Override
    public void append(
            LlmConversationKey key,
            LlmConversationTurn turn
    ) {
        Deque<LlmConversationTurn> turns = conversations.computeIfAbsent(
                        key,
                        ignored -> new ArrayDeque<>()
                );

        synchronized (turns) {
            turns.addLast(turn);

            while (turns.size() > MAX_TURNS) {
                turns.removeFirst();
            }
        }
    }

    @Override
    public void clear(
            LlmConversationKey key
    ) {
        conversations.remove(key);
    }
}
