package ru.reik.smarthome.orchestrator.dto.telegram;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TelegramFileResponse(
        boolean ok,
        TelegramFileResult result
) {
    public record TelegramFileResult(
            @JsonProperty("file_id")
            String fileId,

            @JsonProperty("file_unique_id")
            String fileUniqueId,

            @JsonProperty("file_size")
            Long fileSize,

            @JsonProperty("file_path")
            String filePath
    ) {
    }
}
