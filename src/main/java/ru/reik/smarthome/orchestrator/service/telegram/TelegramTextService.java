package ru.reik.smarthome.orchestrator.service.telegram;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TelegramTextService {
    public String handleMessage(Map<String, Object> message) {
        return (String) message.get("text");
    }
}
