package ru.reik.smarthome.orchestrator.service.assistant.client;

import org.springframework.stereotype.Component;
import ru.reik.smarthome.orchestrator.dto.assistant.AssistantClientType;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class AssistantRequestHandlerRegistry {
    private final Map<
                AssistantClientType,
                AssistantRequestHandler
                > handlers;

    public AssistantRequestHandlerRegistry(
            List<AssistantRequestHandler> handlers
    ) {
        this.handlers = handlers.stream()
                .collect(Collectors.toUnmodifiableMap(
                        AssistantRequestHandler::clientType,
                        Function.identity()
                ));
    }

    public AssistantRequestHandler get(
            AssistantClientType clientType
    ) {
        AssistantRequestHandler handler = handlers.get(clientType);

        if (handler == null) {
            throw new IllegalArgumentException(
                    "Не найден обработчик клиента: "
                            + clientType
            );
        }

        return handler;
    }
}
