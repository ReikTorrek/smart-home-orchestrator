package ru.reik.smarthome.orchestrator.client.llm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import ru.reik.smarthome.orchestrator.config.llm.LlmProperties;
import ru.reik.smarthome.orchestrator.dto.llm.LlmChatRequest;
import ru.reik.smarthome.orchestrator.dto.llm.LlmChatResponse;
import ru.reik.smarthome.orchestrator.dto.llm.LlmMessage;

import java.util.List;

@Component
public class OpenAiCompatibleLlmClient implements LlmClient {
    private static final Logger log =
            LoggerFactory.getLogger(
                    OpenAiCompatibleLlmClient.class
            );

    private final RestClient restClient;
    private final LlmProperties llmProperties;

    public OpenAiCompatibleLlmClient(
            @Qualifier("llmClient") RestClient restClient,
            LlmProperties llmProperties) {
        this.restClient = restClient;
        this.llmProperties = llmProperties;
    }

    @Override
    public String generateJson(List<LlmMessage> messages) {
        llmProperties.validateConfigured();

        LlmChatRequest request = new LlmChatRequest(
                llmProperties.model(),
                messages,
                new LlmChatRequest.ResponseFormat("json_object"),
                llmProperties.temperature(),
                new LlmChatRequest.Provider(
                        true,
                        "deny",
                        true
                )
        );

        try {
            LlmChatResponse response = restClient.post()
                    .uri("/chat/completions")
                    .body(request)
                    .retrieve()
                    .body(LlmChatResponse.class);

            return extractContent(response);
        } catch (RestClientResponseException exception) {
            log.error(
                    "LLM request failed, status={}, body={}",
                    exception.getStatusCode(),
                    exception.getResponseBodyAsString(),
                    exception
            );

            throw new IllegalStateException(
                    "LLM API вернул ошибку: "
                            + exception.getStatusCode(),
                    exception
            );
        }
    }

    private String extractContent(
            LlmChatResponse response
    ) {
        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new IllegalStateException("LLM API вернул пустой список ответов");
        }

        LlmChatResponse.Choice choice = response.choices().getFirst();

        if (choice.message() == null || choice.message().content() == null || choice.message().content().isBlank()) {
            throw new IllegalStateException("LLM API не вернул содержимое ответа");
        }

        log.info(
                "LLM response received, requestedModel={}, actualModel={}",
                llmProperties.model(),
                response.model()
        );

        return choice.message().content();
    }
}
