package ru.reik.smarthome.orchestrator.service.homeassistant;

import org.springframework.stereotype.Component;
import ru.reik.smarthome.orchestrator.dto.homeassistant.HomeAssistantState;
import ru.reik.smarthome.orchestrator.dto.homeassistant.HomeAssistantStateAttributes;

import java.util.List;

@Component
public class HomeAssistantAttributeValueResolver {
    public List<String> resolveStringList(
            HomeAssistantState state,
            String attributeName
    ) {
        if (attributeName == null || attributeName.isBlank()) {
            return List.of();
        }

        HomeAssistantStateAttributes attributes = state.attributes();

        if (attributes == null) {
            return List.of();
        }

        List<String> values = switch (attributeName) {
            case "effectList" -> attributes.effectList();

            default -> throw new IllegalArgumentException(
                    "Неизвестный атрибут допустимых значений: "
                            + attributeName
            );
        };

        return values == null
                ? List.of()
                : List.copyOf(values);
    }
}
