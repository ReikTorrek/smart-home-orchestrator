package ru.reik.smarthome.orchestrator.service.telegram;

import org.springframework.stereotype.Service;
import ru.reik.smarthome.orchestrator.client.SpeechToTextClient;
import ru.reik.smarthome.orchestrator.client.TelegramClient;
import ru.reik.smarthome.orchestrator.dto.telegram.TelegramFileResponse;

import java.util.Map;

@Service
public class TelegramVoiceService {
    private final TelegramClient telegramClient;
    private final SpeechToTextClient speechToTextClient;

    public TelegramVoiceService(
            TelegramClient telegramClient,
            SpeechToTextClient speechToTextClient
    ) {
        this.telegramClient = telegramClient;
        this.speechToTextClient = speechToTextClient;
    }

    @SuppressWarnings("unchecked")
    public String handleMessage(Map<String, Object> message) {
        Map<String, Object> voice = (Map<String, Object>) message.get("voice");
        String mime = voice.get("mime_type").toString();
        String fileId =  (String) voice.get("file_id");

        TelegramFileResponse fileResponse = telegramClient.getFile(fileId);
        byte[] audio = telegramClient.downloadFile(fileResponse.result().filePath());

        return speechToTextClient.transcribe(audio, mime);
    }
}
