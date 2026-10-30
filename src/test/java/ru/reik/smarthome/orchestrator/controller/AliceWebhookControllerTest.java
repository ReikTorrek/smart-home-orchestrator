package ru.reik.smarthome.orchestrator.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.reik.smarthome.orchestrator.dto.assistant.AssistantClientType;
import ru.reik.smarthome.orchestrator.dto.assistant.AssistantRequest;
import ru.reik.smarthome.orchestrator.dto.assistant.AssistantResponse;
import ru.reik.smarthome.orchestrator.service.assistant.AssistantOrchestratorService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AliceWebhookControllerTest {
    private final AssistantOrchestratorService orchestrator = mock(AssistantOrchestratorService.class);
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(
                new AliceWebhookController(orchestrator, "test-secret", "test-skill")
        ).build();
    }

    @Test
    void greetsOnLaunchWithoutCallingTheAssistant() throws Exception {
        mvc.perform(post("/api/v1/alice-webhook/test-secret")
                        .contentType(MediaType.APPLICATION_JSON).content(request("")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value("1.0"))
                .andExpect(jsonPath("$.response.text").value("Привет. Я Рин. Слушаю тебя."))
                .andExpect(jsonPath("$.response.end_session").value(false))
                .andExpect(jsonPath("$.response.tts").doesNotExist());

        verifyNoInteractions(orchestrator);
    }

    @Test
    void forwardsTheCommandAndKeepsTheAliceConversation() throws Exception {
        when(orchestrator.handle(any())).thenReturn(AssistantResponse.text("В комнате 23 градуса."));

        mvc.perform(post("/api/v1/alice-webhook/test-secret")
                        .contentType(MediaType.APPLICATION_JSON).content(request("Какая температура?")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.text").value("В комнате 23 градуса."));

        ArgumentCaptor<AssistantRequest> captor = ArgumentCaptor.forClass(AssistantRequest.class);
        verify(orchestrator).handle(captor.capture());
        assertEquals(new AssistantRequest(AssistantClientType.AUDIO, "alice:test-session",
                "Какая температура?", "alice"), captor.getValue());
    }

    @Test
    void rejectsTheWrongSecret() throws Exception {
        mvc.perform(post("/api/v1/alice-webhook/wrong-secret")
                        .contentType(MediaType.APPLICATION_JSON).content(request("Включи свет")))
                .andExpect(status().isNotFound());

        verifyNoInteractions(orchestrator);
    }

    @Test
    void rejectsAnotherSkill() throws Exception {
        mvc.perform(post("/api/v1/alice-webhook/test-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request("Включи свет").replace("test-skill", "other-skill")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(orchestrator);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{}",
            "{\"request\":null,\"session\":{\"session_id\":\"test-session\",\"skill_id\":\"test-skill\",\"message_id\":0},\"version\":\"1.0\"}",
            "{\"request\":{\"type\":\"SimpleUtterance\",\"command\":\"Включи свет\"},\"session\":{\"skill_id\":\"test-skill\",\"message_id\":0},\"version\":\"1.0\"}",
            "{\"request\":{\"type\":\"SimpleUtterance\",\"command\":\"Включи свет\"},\"session\":{\"session_id\":\" \",\"skill_id\":\"test-skill\",\"message_id\":0},\"version\":\"1.0\"}"
    })
    void rejectsIncompleteRequestsBeforeCallingTheAssistant(String body) throws Exception {
        mvc.perform(post("/api/v1/alice-webhook/test-secret")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orchestrator);
    }

    @ParameterizedTest
    @ValueSource(strings = {"\"message_id\":-1", "\"message_id\":null", "\"version\":\"2.0\"", "\"version\":null", "\"command\":null"})
    void rejectsInvalidProtocolFields(String field) throws Exception {
        String body = request("Включи свет");
        if (field.startsWith("\"message_id\"")) {
            body = body.replace("\"message_id\":0", field);
        } else if (field.startsWith("\"version\"")) {
            body = body.replace("\"version\":\"1.0\"", field);
        } else {
            body = body.replace("\"command\":\"Включи свет\"", field);
        }

        mvc.perform(post("/api/v1/alice-webhook/test-secret")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orchestrator);
    }

    @Test
    void returnsAnAliceResponseWhenPlanningFails() throws Exception {
        when(orchestrator.handle(any())).thenThrow(new IllegalStateException("LLM unavailable"));

        mvc.perform(post("/api/v1/alice-webhook/test-secret")
                        .contentType(MediaType.APPLICATION_JSON).content(request("Какая температура?")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value("1.0"))
                .andExpect(jsonPath("$.response.text").value("Не удалось подтвердить результат запроса."))
                .andExpect(jsonPath("$.response.end_session").value(false));
    }

    @Test
    void boundsTheSerializedAssistantAnswer() throws Exception {
        when(orchestrator.handle(any())).thenReturn(AssistantResponse.text("а".repeat(1100)));

        mvc.perform(post("/api/v1/alice-webhook/test-secret")
                        .contentType(MediaType.APPLICATION_JSON).content(request("Какая температура?")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.text").value("а".repeat(1023) + "…"));
    }

    private String request(String command) {
        return """
                {"meta":{"locale":"ru-RU"},
                 "request":{"type":"SimpleUtterance","command":"%s","nlu":{"tokens":[]}},
                 "session":{"session_id":"test-session","message_id":0,"skill_id":"test-skill","new":true},
                 "version":"1.0"}
                """.formatted(command);
    }
}
