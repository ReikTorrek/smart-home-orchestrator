package ru.reik.smarthome.orchestrator.service.homeassistant;

import org.springframework.stereotype.Service;
import ru.reik.smarthome.orchestrator.dto.smarthome.ActionParameterDefinition;
import ru.reik.smarthome.orchestrator.dto.smarthome.ActionParameterType;
import ru.reik.smarthome.orchestrator.dto.smarthome.SmartHomeAction;

import java.util.List;
import java.util.Map;

@Service
public class HomeAssistantActionValidator {

    public void validate(
            SmartHomeAction action,
            Map<String, Object> parameters
    ) {
        Map<String, Object> actualParameters =
                parameters == null
                        ? Map.of()
                        : parameters;

        validateUnknownParameters(
                action,
                actualParameters
        );

        for (
                Map.Entry<String, ActionParameterDefinition> entry
                : action.parameters().entrySet()
        ) {
            validateParameter(
                    entry.getKey(),
                    entry.getValue(),
                    actualParameters
            );
        }
    }

    private void validateUnknownParameters(
            SmartHomeAction action,
            Map<String, Object> parameters
    ) {
        List<String> unknownParameters =
                parameters.keySet().stream()
                        .filter(parameter ->
                                !action.parameters()
                                        .containsKey(parameter)
                        )
                        .sorted()
                        .toList();

        if (!unknownParameters.isEmpty()) {
            throw new IllegalArgumentException(
                    "Недопустимые параметры для действия '%s': %s"
                            .formatted(
                                    action.code(),
                                    String.join(
                                            ", ",
                                            unknownParameters
                                    )
                            )
            );
        }
    }

    private void validateParameter(
            String name,
            ActionParameterDefinition definition,
            Map<String, Object> parameters
    ) {
        Object value = parameters.get(name);

        if (value == null) {
            if (definition.required()) {
                throw new IllegalArgumentException(
                        "Не передан обязательный параметр: "
                                + name
                );
            }

            return;
        }

        validateType(
                name,
                value,
                definition.type()
        );

        if (value instanceof Number number) {
            validateNumberRange(
                    name,
                    number,
                    definition
            );
        }
    }

    private void validateType(
            String name,
            Object value,
            ActionParameterType type
    ) {
        boolean valid = switch (type) {
            case NUMBER -> value instanceof Number;
            case STRING -> value instanceof String;
            case BOOLEAN -> value instanceof Boolean;
        };

        if (!valid) {
            throw new IllegalArgumentException(
                    "Параметр '%s' должен иметь тип %s"
                            .formatted(name, type)
            );
        }
    }

    private void validateNumberRange(
            String name,
            Number number,
            ActionParameterDefinition definition
    ) {
        double value = number.doubleValue();

        if (
                definition.minimum() != null
                        && value < definition.minimum()
        ) {
            throw new IllegalArgumentException(
                    "Параметр '%s' не может быть меньше %s"
                            .formatted(
                                    name,
                                    definition.minimum()
                            )
            );
        }

        if (
                definition.maximum() != null
                        && value > definition.maximum()
        ) {
            throw new IllegalArgumentException(
                    "Параметр '%s' не может быть больше %s"
                            .formatted(
                                    name,
                                    definition.maximum()
                            )
            );
        }
    }
}