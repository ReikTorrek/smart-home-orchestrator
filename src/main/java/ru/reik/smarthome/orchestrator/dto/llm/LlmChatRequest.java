package ru.reik.smarthome.orchestrator.dto.llm;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record LlmChatRequest(
        String model,
        List<LlmMessage> messages,

        @JsonProperty("response_format")
        ResponseFormat responseFormat,

        double temperature,
        Provider provider
) {
    public record Message(
            String role,
            String content
    ) {
    }

    public record ResponseFormat(
            String type
    ) {
    }

    public record Provider(
            @JsonProperty("require_parameters")
            boolean requireParameters,

            @JsonProperty("data_collection")
            String dataCollection,

            boolean zdr
    ) {
    }
}