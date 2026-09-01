package ru.reik.smarthome.orchestrator.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import ru.reik.smarthome.orchestrator.config.homeassistant.HomeAssistantProperties;
import ru.reik.smarthome.orchestrator.config.llm.LlmProperties;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;

@Configuration
public class HttpClientConfig {

    @Bean
    @Qualifier("telegramRestClient")
    public RestClient telegramRestClient(TelegramBotProperties telegramBotProperties, ExternalProxyProperties proxyProperties) {
        return RestClient.builder()
                .requestFactory(createExternalRequestFactory(proxyProperties))
                .baseUrl(telegramBotProperties.apiUrl() + telegramBotProperties.token())
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
    public RestClient llmClient(LlmProperties  llmProperties, ExternalProxyProperties proxyProperties) {
        return RestClient.builder()
                .requestFactory(createExternalRequestFactory(proxyProperties))
                .baseUrl(llmProperties.baseUrl())
                .defaultHeaders(headers -> {
                    headers.setBearerAuth(llmProperties.apiKey());
                    headers.setContentType(MediaType.APPLICATION_JSON);
                })
                .build();
    }

    private SimpleClientHttpRequestFactory createExternalRequestFactory(ExternalProxyProperties proxyProperties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(60));

        if (proxyProperties.enabled()) {
            InetSocketAddress address = new InetSocketAddress(proxyProperties.host(), proxyProperties.port());
            Proxy proxy = new Proxy(Proxy.Type.SOCKS, address);

            factory.setProxy(proxy);
        }

        return factory;
    }
}
