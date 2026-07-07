package ru.reik.smarthome.orchestrator.service;

import org.springframework.stereotype.Service;
import ru.reik.smarthome.orchestrator.client.HomeAssistantClient;
import ru.reik.smarthome.orchestrator.dto.AssistantAction;
import ru.reik.smarthome.orchestrator.dto.AssistantResponse;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class CommandService {
    private final HomeAssistantClient homeAssistantClient;

    public CommandService(HomeAssistantClient homeAssistantClient) {
        this.homeAssistantClient = homeAssistantClient;
    }

    public AssistantResponse handle(String text) {
        String normalized = text.toLowerCase(Locale.ROOT).trim();

        if (containsAny(normalized, "нормальный", "включи")) {
            homeAssistantClient.testOn();
            return new AssistantResponse(
                    "Ok, Делаю нормальный свет",
                    List.of(new AssistantAction(
                            "MAKE_LIGHT_COMFORTABLE",
                            Map.of("room", "bedroom", "brightnessPct", 45)
                    ))
            );
        }

        if (containsAny(normalized, "выключи свет", "свет выключи")) {
            homeAssistantClient.testOff();
            return new AssistantResponse(
                    "Понял. Пока это mock-режим: выбрал выключение света.",
                    List.of(new AssistantAction(
                            "TURN_LIGHT_OFF",
                            Map.of("room", "bedroom")
                    ))
            );
        }

        return new AssistantResponse(
                "Я получил команду, но пока не знаю, что с ней делать.",
                List.of()
        );
    }

    private boolean containsAny(String text, String... phrases) {
        for (String phrase : phrases) {
            if (text.contains(phrase)) {
                return true;
            }
        }
        return false;
    }
}
