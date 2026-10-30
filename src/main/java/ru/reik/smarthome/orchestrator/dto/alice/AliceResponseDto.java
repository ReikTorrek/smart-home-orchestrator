package ru.reik.smarthome.orchestrator.dto.alice;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public record AliceResponseDto(
        Response response,
        String version
) {
    private static final int MAX_TEXT_LENGTH = 1024;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Response(
            String text,
            String tts,

            @JsonProperty("end_session")
            boolean endSession
    ) {}

    public static AliceResponseDto speak(String text, boolean endSession) {
        text = text == null || text.isBlank() ? "Не удалось сформировать ответ." : text.strip();

        if (text.length() > MAX_TEXT_LENGTH) {
            int end = MAX_TEXT_LENGTH - 1;
            if (Character.isHighSurrogate(text.charAt(end - 1))) {
                end--;
            }
            text = text.substring(0, end) + "…";
        }

        return new AliceResponseDto(new Response(text, null, endSession), "1.0");
    }
}
