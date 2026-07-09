package ru.reik.smarthome.orchestrator.service.telegram;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import ru.reik.smarthome.orchestrator.client.TelegramClient;
import ru.reik.smarthome.orchestrator.dto.telegram.TelegramBotCommand;

import java.util.List;

@Service
public class TelegramRegisterCommandService {
    private static final Logger log = LoggerFactory.getLogger(TelegramRegisterCommandService.class);

    private final TelegramClient telegramClient;

    public TelegramRegisterCommandService(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void registerCommands() {
        telegramClient.registerCommands(List.of(
                new TelegramBotCommand(
                        "ha_entities",
                        "Сущности"
                ),
                new TelegramBotCommand(
                        "ha_do",
                        "Выполнить команду"
                ),
                new TelegramBotCommand(
                        "ha_refresh",
                        "Обновить сущности"
                )
        ));

        log.info("Tg commands have been registered");
    }
}
