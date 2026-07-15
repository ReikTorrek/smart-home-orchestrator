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

        switch (definition.type()) {
            case NUMBER -> {
                assert value instanceof Number;
                validateNumberRange(name, (Number) value, definition);
            }
            case NUMBER_LIST -> validateNumberList(name, value, definition);
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
            case NUMBER_LIST -> isNumberList(value);
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

        if (definition.minimum() != null && value < definition.minimum()) {
            throw new IllegalArgumentException(
                    "Параметр '%s' не может быть меньше %s"
                            .formatted(
                                    name,
                                    definition.minimum()
                            )
            );
        }

        if (definition.maximum() != null && value > definition.maximum()) {
            throw new IllegalArgumentException(
                    "Параметр '%s' не может быть больше %s"
                            .formatted(
                                    name,
                                    definition.maximum()
                            )
            );
        }
    }

    private void validateNumberList(
            String name,
            Object value,
            ActionParameterDefinition definition
    ) {
        if (!(value instanceof List<?> list)) {
            return;
        }

        validateListSize(
                name,
                list,
                definition
        );

        for (int index = 0; index < list.size(); index++) {
            Number number = (Number) list.get(index);

            validateListItemRange(
                    name,
                    index,
                    number,
                    definition
            );
        }
    }

    private void validateListSize(
            String name,
            List<?> values,
            ActionParameterDefinition definition
    ) {
        if (definition.size() != null && values.size() != definition.size()) {
            throw new IllegalArgumentException(
                    "Параметр '%s' должен содержать %d элементов"
                            .formatted(
                                    name,
                                    definition.size()
                            )
            );
        }
    }

    private void validateListItemRange(
            String name,
            int index,
            Number number,
            ActionParameterDefinition definition
    ) {
        double value = number.doubleValue();

        if (definition.itemMinimum() != null && value < definition.itemMinimum()) {
            throw new IllegalArgumentException(
                    "Элемент %d параметра '%s' не может быть меньше %s"
                            .formatted(
                                    index,
                                    name,
                                    definition.itemMinimum()
                            )
            );
        }

        if (definition.itemMaximum() != null && value > definition.itemMaximum()) {
            throw new IllegalArgumentException(
                    "Элемент %d параметра '%s' не может быть больше %s"
                            .formatted(
                                    index,
                                    name,
                                    definition.itemMaximum()
                            )
            );
        }
    }

    private boolean isNumberList(Object value) {
        if (!(value instanceof List<?> list)) {
            return false;
        }

        return list.stream()
                .allMatch(item -> item instanceof Number);
    }
}