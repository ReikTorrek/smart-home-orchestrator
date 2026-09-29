package ru.reik.smarthome.orchestrator.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class SpeechToTextClient {
    private final RestClient restClient;

    public SpeechToTextClient(@Qualifier("speechToTextRestClient")RestClient restClient) {
        this.restClient = restClient;
    }

    public String transcribe(byte[] audio, String mediaType) {

        ByteArrayResource audioResource = new ByteArrayResource(audio) {
            @Override
            public String getFilename() {
                return "voice.ogg";
            }
        };

        HttpHeaders fileHeaders = new HttpHeaders();
        fileHeaders.setContentType(
                MediaType.parseMediaType(mediaType)
        );

        HttpEntity<ByteArrayResource> filePart =
                new HttpEntity<>(audioResource, fileHeaders);

        MultiValueMap<String, Object> body =
                new LinkedMultiValueMap<>();

        body.add("file", filePart);

        Map response = restClient.post()
                .uri("/transcribe")
                .body(body)
                .retrieve()
                .body(Map.class);

//        if (response == null
//                || response.text() == null
//                || response.text().isBlank()) {
//
//            throw new IllegalStateException(
//                    "STT returned empty transcription"
//            );
//        }

        return response.get("text").toString();
    }
}
