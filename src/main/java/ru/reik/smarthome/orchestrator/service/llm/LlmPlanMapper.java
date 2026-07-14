package ru.reik.smarthome.orchestrator.service.llm;

import org.springframework.stereotype.Component;
import ru.reik.smarthome.orchestrator.dto.assistant.AssistantResponse;
import ru.reik.smarthome.orchestrator.dto.homeassistant.HomeAssistantActionPayload;
import ru.reik.smarthome.orchestrator.dto.llm.LlmPlan;
import ru.reik.smarthome.orchestrator.dto.smarthome.SmartHomeAction;
import ru.reik.smarthome.orchestrator.dto.smarthome.SmartHomeEntity;
import ru.reik.smarthome.orchestrator.service.CatalogService;
import ru.reik.smarthome.orchestrator.service.homeassistant.HomeAssistantActionValidator;

import java.util.List;

@Component
public class LlmPlanMapper {
    private static final int MAX_ACTION = 10;

    private final CatalogService catalogService;
    private final HomeAssistantActionValidator actionValidator;

    public LlmPlanMapper(CatalogService catalogService, HomeAssistantActionValidator actionValidator) {
        this.catalogService = catalogService;
        this.actionValidator = actionValidator;
    }

    public AssistantResponse map(LlmPlan plan) {
        if (plan == null) {
            throw new IllegalArgumentException("Llm plan is null");
        }

        if (plan.actions().size() > MAX_ACTION) {
            throw new IllegalArgumentException("Llm plan actions are larger than " + MAX_ACTION);
        }

        List<HomeAssistantActionPayload> actions = plan.actions().stream().map(this::mapAction).toList();

        return AssistantResponse.withActions(
                plan.answer(),
                actions
        );
    }

    private HomeAssistantActionPayload mapAction(LlmPlan.Action planAction) {
        SmartHomeEntity entity = findEntity(planAction.entityId());
        SmartHomeAction action = findAction(entity, planAction.actionCode());
        actionValidator.validate(action, planAction.parameters());

        return new HomeAssistantActionPayload(
                entity.entityId(),
                action.code(),
                planAction.parameters()
        );
    }

    private SmartHomeEntity findEntity(String entityId) {
        return catalogService.getEntities().stream()
                .filter(entity -> entity.entityId().equals(entityId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No entity with id " + entityId));
    }

    private SmartHomeAction findAction(SmartHomeEntity entity, String actionCode) {
        return entity.actions().stream()
                .filter(action -> action.code().equals(actionCode))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No action with code " + actionCode));
    }

    private String normalizeAnswer(String answer) {
        if (answer == null || answer.isBlank()) {
            return "Подготовил план действий.";
        }

        return answer.trim();
    }
}
