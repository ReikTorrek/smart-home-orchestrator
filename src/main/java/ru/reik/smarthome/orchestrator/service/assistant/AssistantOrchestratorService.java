package ru.reik.smarthome.orchestrator.service.assistant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.reik.smarthome.orchestrator.dto.assistant.AssistantResponse;
import ru.reik.smarthome.orchestrator.service.CommandService;

@Service
public class AssistantOrchestratorService {
    private static final Logger log = LoggerFactory.getLogger(AssistantOrchestratorService.class);

    private final CommandService commandService;
    private final AssistantActionExecutor assistantActionExecutor;

    public AssistantOrchestratorService(CommandService commandService, AssistantActionExecutor assistantActionExecutor) {
        this.commandService = commandService;
        this.assistantActionExecutor = assistantActionExecutor;
    }

    public AssistantResponse handle(String text) {
        AssistantResponse response = commandService.handle(text);

        try {
            assistantActionExecutor.executeAll(response.actions());

            return response;
        } catch (IllegalArgumentException exception) {
            return AssistantResponse.text(
                    exception.getMessage()
            );
        } catch (Exception exception) {
            log.error(
                    "Assistant action execution failed",
                    exception
            );

            return AssistantResponse.text(
                    "Не удалось выполнить действие в Home Assistant."
            );
        }
    }
}
