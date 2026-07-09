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
                .body(new ParameterizedTypeReference<List<HomeAssistantState>>() {});
    }

    public void testOn() {
        Map<String, Object> body = Map.of("entity_id", "light.zb_5");
        restClient.post()
                .uri(uriBuilder -> uriBuilder.path("/api/services/light/turn_on").build())
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    public void testOff() {
        Map<String, Object> body = Map.of("entity_id", "light.zb_5");
        restClient.post()
                .uri(uriBuilder -> uriBuilder.path("/api/services/light/turn_off").build())
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }
}
