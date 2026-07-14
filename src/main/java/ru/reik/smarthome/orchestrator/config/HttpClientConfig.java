package ru.reik.smarthome.orchestrator.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import ru.reik.smarthome.orchestrator.config.homeassistant.HomeAssistantProperties;
import ru.reik.smarthome.orchestrator.config.llm.LlmProperties;

@Configuration
public class HttpClientConfig {

    @Bean
    @Qualifier("telegramRestClient")
    public RestClient telegramRestClient(TelegramBotProperties telegramBotProperties) {
        return RestClient.builder()
                .baseUrl("https://api.telegram.org/bot" + telegramBotProperties.token())
                .build();
    }

    @Bean
    @Qualifier("homeAssistantClient")
    public RestClient homeAssistantRestClient(HomeAssistantProperties homeAssistantProperties) {
        return RestClient.builder()
                .baseUrl(homeAssistantProperties.baseUrl())
                .defaultHeaders(headers -> {
                    headers.setBearerAuth(homeAssistantProperties.longLivedToken());
                    headers.setContentType(MediaType.APPLICATION_JSON);
                })
                .build();
    }

    @Bean
    @Qualifier("llmClient")
    public RestClient llmClient(LlmProperties  llmProperties) {
        return RestClient.builder()
                .baseUrl(llmProperties.baseUrl())
                .defaultHeaders(headers -> {
                    headers.setBearerAuth(llmProperties.apiKey());
                    headers.setContentType(MediaType.APPLICATION_JSON);
                })
                .build();
    }
}
