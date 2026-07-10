package ru.reik.smarthome.orchestrator.service.homeassistant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.reik.smarthome.orchestrator.client.HomeAssistantClient;
import ru.reik.smarthome.orchestrator.dto.homeassistant.HomeAssistantActionPayload;
import ru.reik.smarthome.orchestrator.dto.smarthome.SmartHomeAction;
import ru.reik.smarthome.orchestrator.dto.smarthome.SmartHomeEntity;
import ru.reik.smarthome.orchestrator.service.CatalogService;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HomeAssistantActionServiceTest {
    @Mock
    private HomeAssistantClient homeAssistantClient;

    @Mock
    private CatalogService catalogService;

    @InjectMocks
    private HomeAssistantActionService homeAssistantActionService;

    @Test
    void executeShouldPassParametersToHomeAssistantClient() {
        // Arrange — создаём действие, доступное лампочке.
        SmartHomeAction action = new SmartHomeAction(
                "set_brightness",
                "Установить яркость",
                "light",
                "turn_on",
                Map.of("transition", 1)
        );

        // Создаём сущность, которая будет находиться в каталоге.
        SmartHomeEntity entity = new SmartHomeEntity(
                "light.bedroom",
                "light",
                "Свет в спальне",
                "on",
                List.of(action)
        );

        // Объясняем mock-объекту:
        // когда сервис запросит каталог, верни нашу лампочку.
        when(catalogService.getEntities())
                .thenReturn(List.of(entity));

        HomeAssistantActionPayload payload =
                new HomeAssistantActionPayload(
                        "light.bedroom",
                        "set_brightness",
                        Map.of(
                                "brightness_pct", 35,
                                "transition", 2
                        )
                );

        // Act — выполняем проверяемый метод.
        String result = homeAssistantActionService.execute(payload);

        // Assert — проверяем, что клиент HA был вызван
        // с правильным domain, service и body.
        verify(homeAssistantClient).callService(
                "light",
                "turn_on",
                Map.of(
                        "entity_id", "light.bedroom",
                        "brightness_pct", 35,
                        "transition", 2
                )
        );

        // Дополнительно проверяем текстовый результат.
        assertEquals(
                "Выполнено: Свет в спальне → Установить яркость",
                result
        );
    }

    @Test
    void executeShouldNotAllowEntityIdOverride() {
        // Arrange
        SmartHomeAction action = new SmartHomeAction(
                "turn_on",
                "Включить",
                "light",
                "turn_on",
                Map.of()
        );

        SmartHomeEntity entity = new SmartHomeEntity(
                "light.bedroom",
                "light",
                "Свет в спальне",
                "off",
                List.of(action)
        );

        when(catalogService.getEntities())
                .thenReturn(List.of(entity));

        HomeAssistantActionPayload payload =
                new HomeAssistantActionPayload(
                        "light.bedroom",
                        "turn_on",
                        Map.of(
                                "entity_id",
                                "light.some_other_light"
                        )
                );

        // Act
        homeAssistantActionService.execute(payload);

        // Assert
        verify(homeAssistantClient).callService(
                "light",
                "turn_on",
                Map.of(
                        "entity_id",
                        "light.bedroom"
                )
        );
    }

    @Test
    void executeShouldThrowExceptionForUnknownEntity() {
        // Arrange
        when(catalogService.getEntities())
                .thenReturn(List.of());

        HomeAssistantActionPayload payload =
                new HomeAssistantActionPayload(
                        "light.unknown",
                        "turn_on",
                        Map.of()
                );

        // Act
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> homeAssistantActionService.execute(payload)
        );

        // Assert
        assertEquals(
                "Unknown entityId: light.unknown",
                exception.getMessage()
        );

        verifyNoInteractions(homeAssistantClient);
    }

    @Test
    void executeShouldThrowExceptionForUnavailableAction() {
        // Arrange
        SmartHomeAction action = new SmartHomeAction(
                "turn_on",
                "Включить",
                "light",
                "turn_on",
                Map.of()
        );

        SmartHomeEntity entity = new SmartHomeEntity(
                "light.bedroom",
                "light",
                "Свет в спальне",
                "off",
                List.of(action)
        );

        when(catalogService.getEntities())
                .thenReturn(List.of(entity));

        HomeAssistantActionPayload payload =
                new HomeAssistantActionPayload(
                        "light.bedroom",
                        "set_color",
                        Map.of()
                );

        // Act
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> homeAssistantActionService.execute(payload)
        );

        // Assert
        assertEquals(
                "Действие 'set_color' недоступно для light.bedroom",
                exception.getMessage()
        );

        verifyNoInteractions(homeAssistantClient);
    }
}
