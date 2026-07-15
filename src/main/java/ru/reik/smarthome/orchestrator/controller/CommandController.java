package ru.reik.smarthome.orchestrator.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.reik.smarthome.orchestrator.dto.assistant.AssistantResponse;
import ru.reik.smarthome.orchestrator.dto.UserCommandRequest;
import ru.reik.smarthome.orchestrator.service.telegram.CommandService;

@RestController
@RequestMapping("/api/command")
public class CommandController {
    private final CommandService commandService;

    public CommandController(CommandService commandService) {
        this.commandService = commandService;
    }

    @PostMapping
    public AssistantResponse handle(@Valid @RequestBody UserCommandRequest request) {
        //return commandService.handle(request);
        return AssistantResponse.text("text");
    }
}
