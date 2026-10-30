package ru.reik.smarthome.orchestrator.dto.alice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class AliceResponseDtoTest {
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\n\t"})
    void providesANonblankFallback(String answer) {
        assertFalse(AliceResponseDto.speak(answer, false).response().text().isBlank());
    }

    @Test
    void preservesAnAnswerAtTheLimit() {
        String answer = "а".repeat(1024);
        assertEquals(answer, AliceResponseDto.speak(answer, false).response().text());
    }

    @Test
    void doesNotSplitASurrogatePairWhenTruncating() {
        String answer = "а".repeat(1022) + "😀" + "длинный ответ";
        String text = AliceResponseDto.speak(answer, false).response().text();

        assertEquals("а".repeat(1022) + "…", text);
        assertTrue(text.length() <= 1024);
    }
}
