package ru.reik.smarthome.orchestrator.dto.alice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AliceRequestDto(
        @NotNull @Valid
        Request request,

        @NotNull @Valid
        Session session,

        @NotBlank @Pattern(regexp = "1\\.0")
        String version
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Request(
            @NotBlank
            String type,

            @NotNull @Size(max = 1024)
            String command,

            @JsonProperty("original_utterance")
            String originalUtterance
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Session(
            @NotBlank @Size(max = 64)
            @JsonProperty("session_id")
            String sessionId,

            @NotNull @PositiveOrZero
            @JsonProperty("message_id")
            Integer messageId,

            @NotBlank
            @JsonProperty("skill_id")
            String skillId,

            @JsonProperty("new")
            Boolean newSession,

            Application application,

            User user
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Application(
            @JsonProperty("application_id")
            String applicationId
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record User(
            @JsonProperty("user_id")
            String userId
    ) {}
}
