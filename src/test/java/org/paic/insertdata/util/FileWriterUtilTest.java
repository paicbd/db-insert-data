package org.paic.insertdata.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FileWriterUtilTest {

    private FileWriterUtil fileWriterUtil;

    @BeforeEach
    void setUp() {
        fileWriterUtil = new FileWriterUtil();
    }

    @Test
    void testWrite() throws IOException {
        byte[] bytes = "test data".getBytes();
        Path mockPath = mock(Path.class);
        StandardOpenOption[] options = {StandardOpenOption.CREATE};

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.write(any(Path.class), any(byte[].class), any(StandardOpenOption[].class)))
                    .thenReturn(mockPath);

            fileWriterUtil.write(mockPath, bytes, options);

            mockedFiles.verify(() -> Files.write(mockPath, bytes, options), times(1));
        }
    }

    @Test
    void testNotExists() {
        Path mockPath = mock(Path.class);

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.notExists(mockPath)).thenReturn(true);

            boolean result = fileWriterUtil.notExists(mockPath);

            mockedFiles.verify(() -> Files.notExists(mockPath), times(1));
            assertTrue(result);
        }
    }

    @Test
    void testCreateDirectories() throws IOException {
        Path mockPath = mock(Path.class);

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            fileWriterUtil.createDirectories(mockPath);

            mockedFiles.verify(() -> Files.createDirectories(mockPath), times(1));
        }
    }

    @Test
    void testWriteThrowsIOException() {
        byte[] bytes = "test data".getBytes();
        Path mockPath = mock(Path.class);
        StandardOpenOption[] options = {StandardOpenOption.CREATE};

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.write(any(Path.class), any(byte[].class), any(StandardOpenOption[].class)))
                    .thenThrow(new IOException("Mock IOException"));

            assertThrows(IOException.class, () -> fileWriterUtil.write(mockPath, bytes, options));
        }
    }
}
