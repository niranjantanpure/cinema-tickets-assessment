package uk.gov.dwp.uc.pairtest.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("InvalidPurchaseException Test Suite")
class InvalidPurchaseExceptionTest {

    @Test
    @DisplayName("Default constructor should instantiate without message")
    void defaultConstructor_shouldCreateInstance() {
        InvalidPurchaseException exception = new InvalidPurchaseException();
        assertNull(exception.getMessage());
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    @DisplayName("Message constructor should retain message")
    void messageConstructor_shouldRetainMessage() {
        String msg = "Test invalid purchase error";
        InvalidPurchaseException exception = new InvalidPurchaseException(msg);
        assertEquals(msg, exception.getMessage());
    }

    @Test
    @DisplayName("Message and cause constructor should retain both")
    void messageAndCauseConstructor_shouldRetainBoth() {
        String msg = "Test error with cause";
        Throwable cause = new IllegalArgumentException("Root cause");
        InvalidPurchaseException exception = new InvalidPurchaseException(msg, cause);
        assertEquals(msg, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}
