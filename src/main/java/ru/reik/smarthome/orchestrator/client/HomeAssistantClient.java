package ru.reik.smarthome.orchestrator.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ru.reik.smarthome.orchestrator.dto.homeassistant.HomeAssistantState;

import java.util.List;
import java.util.Map;

@Service
public class HomeAssistantClient {
    private final RestClient restClient;

    public HomeAssistantClient(@Qualifier("homeAssistantClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public List<HomeAssistantState> getStates() {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/states").build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    public void callService(String domain, String service, Map<String, Object> body) {
        String path = "/api/services/%s/%s".formatted(domain, service);
        restClient.post()
                .uri(uriBuilder -> uriBuilder.path(path).build())
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }
}
