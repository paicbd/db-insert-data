package org.paic.insertdata.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FileOperationExceptionTest {

    @Test
    void testFileOperationExceptionConstructor() {
        String expectedMessage = "Test message";
        Throwable expectedCause = new RuntimeException("Test cause");

        FileOperationException exception = new FileOperationException(expectedMessage, expectedCause);

        assertNotNull(exception, "Exception should not be null.");
        assertEquals(expectedMessage, exception.getMessage(), "Exception message should match.");
        assertEquals(expectedCause, exception.getCause(), "Exception cause should match.");
    }
}