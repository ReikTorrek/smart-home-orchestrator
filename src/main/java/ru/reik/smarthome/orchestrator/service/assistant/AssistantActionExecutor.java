package ru.reik.smarthome.orchestrator.service.assistant;

import org.springframework.stereotype.Service;
import ru.reik.smarthome.orchestrator.dto.homeassistant.HomeAssistantActionPayload;
import ru.reik.smarthome.orchestrator.service.homeassistant.HomeAssistantActionService;

import java.util.List;

@Service
public class AssistantActionExecutor {
    private final HomeAssistantActionService homeAssistantActionService;

    public AssistantActionExecutor(HomeAssistantActionService homeAssistantActionService) {
        this.homeAssistantActionService = homeAssistantActionService;
    }

    public void executeAll(List<HomeAssistantActionPayload> actions) {
        for (HomeAssistantActionPayload action : actions) {
            homeAssistantActionService.execute(action);
        }
    }
}
