package ru.reik.smarthome.orchestrator.service.assistant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.reik.smarthome.orchestrator.dto.assistant.ActionExecutionReport;
import ru.reik.smarthome.orchestrator.dto.assistant.ActionExecutionResult;
import ru.reik.smarthome.orchestrator.dto.homeassistant.HomeAssistantActionPayload;
import ru.reik.smarthome.orchestrator.service.homeassistant.HomeAssistantActionService;

import java.util.ArrayList;
import java.util.List;

@Service
public class AssistantActionExecutor {
    private static final Logger log = LoggerFactory.getLogger(AssistantActionExecutor.class);
    private final HomeAssistantActionService homeAssistantActionService;

    public AssistantActionExecutor(HomeAssistantActionService homeAssistantActionService) {
        this.homeAssistantActionService = homeAssistantActionService;
    }

    public ActionExecutionReport executeAll(List<HomeAssistantActionPayload> actions) {
        List<ActionExecutionResult> results = new ArrayList<>();

        for (HomeAssistantActionPayload action : actions) {
            results.add(execute(action));
        }

        return new ActionExecutionReport(results);
    }

    public ActionExecutionResult execute(HomeAssistantActionPayload action) {
        try {
            homeAssistantActionService.execute(action);

            return ActionExecutionResult.success(action);
        } catch (Exception exception) {
            log.error(
                    "Failed to execute Home Assistant action: {}",
                    action,
                    exception
            );

            return ActionExecutionResult.failure(
                    action,
                    getErrorMessage(exception)
            );
        }
    }

    private String getErrorMessage(Exception exception) {
        if (
                exception.getMessage() == null
                        || exception.getMessage().isBlank()
        ) {
            return "Неизвестная ошибка выполнения";
        }

        return exception.getMessage();
    }
}
