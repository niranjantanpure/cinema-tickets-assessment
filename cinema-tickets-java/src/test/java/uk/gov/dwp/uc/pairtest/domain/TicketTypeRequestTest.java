package uk.gov.dwp.uc.pairtest.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@DisplayName("TicketTypeRequest Domain Model Test Suite")
class TicketTypeRequestTest {

    @Test
    @DisplayName("Constructor and getters should correctly return initialized values")
    void constructorAndGetters_shouldReturnExpectedValues() {
        TicketTypeRequest request = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 3);

        assertEquals(TicketTypeRequest.Type.ADULT, request.getTicketType());
        assertEquals(3, request.getNoOfTickets());
    }

    @ParameterizedTest
    @EnumSource(TicketTypeRequest.Type.class)
    @DisplayName("TicketTypeRequest should support all enum types")
    void supportsAllTicketTypes(TicketTypeRequest.Type type) {
        TicketTypeRequest request = new TicketTypeRequest(type, 1);
        assertEquals(type, request.getTicketType());
    }

    @Test
    @DisplayName("All fields must be private and final to guarantee immutability")
    void fieldsMustBePrivateAndFinal() {
        Field[] fields = TicketTypeRequest.class.getDeclaredFields();

        assertTrue(fields.length > 0, "TicketTypeRequest should have declared fields");

        for (Field field : fields) {
            // Ignore synthetic fields (like coverage agents) if any
            if (field.isSynthetic()) {
                continue;
            }
            int modifiers = field.getModifiers();
            assertTrue(Modifier.isPrivate(modifiers), "Field " + field.getName() + " should be private");
            assertTrue(Modifier.isFinal(modifiers), "Field " + field.getName() + " should be final");
        }
    }

    @Test
    @DisplayName("Class must not expose any setter methods")
    void noSettersShouldBeExposed() {
        Method[] methods = TicketTypeRequest.class.getDeclaredMethods();

        boolean hasSetter = Arrays.stream(methods)
                .anyMatch(m -> m.getName().startsWith("set"));

        assertFalse(hasSetter, "TicketTypeRequest should not expose setter methods");
    }
}
