package ru.reik.smarthome.orchestrator.service.llm;

import org.springframework.stereotype.Component;
import ru.reik.smarthome.orchestrator.dto.smarthome.SmartHomeEntity;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Component
public class LlmPromptFactory {
    private final ObjectMapper objectMapper;

    public LlmPromptFactory(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String buildSystemPrompt() {
        return """
                Ты — планировщик действий умного дома.

                Тебе будет передан каталог доступных сущностей,
                действий и параметров, а также команда пользователя.

                Правила:
                1. Используй только entityId из каталога.
                2. Используй только действия, доступные выбранной сущности.
                3. Используй только параметры, описанные у действия.
                4. Соблюдай типы и допустимые диапазоны параметров.
                5. Не придумывай сущности, действия и параметры.
                6. Возвращай не более 10 действий.
                7. Если выполнять ничего не нужно, верни пустой actions.
                8. Поле answer должно быть коротким и на русском языке.
                9. Не утверждай, что действия уже успешно выполнены.
                10. Верни только JSON без Markdown и пояснений.

                Обязательный формат ответа:
                {
                  "answer": "текст ответа пользователю",
                  "actions": [
                    {
                      "entityId": "точный entityId",
                      "actionCode": "точный код действия",
                      "parameters": {}
                    }
                  ]
                }
                """;
    }

    public String buildUserPrompt(
            String command,
            List<SmartHomeEntity> entities
    ) {
        String catalogJson = objectMapper.writeValueAsString(entities);

        return """
                Каталог умного дома:
                %s

                Команда пользователя:
                %s
                """.formatted(
                catalogJson,
                command
        );
    }
}
