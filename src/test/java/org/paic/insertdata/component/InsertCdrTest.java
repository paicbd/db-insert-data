package org.paic.insertdata.component;

import com.paicbd.smsc.dto.UtilsRecords;
import com.paicbd.smsc.utils.Converter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.paic.insertdata.util.AppProperties;
import org.paic.insertdata.util.FileWriterUtil;
import org.paic.insertdata.util.TransactionHandler;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import redis.clients.jedis.JedisCluster;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;

@Slf4j
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InsertCdrTest {

    @Mock
    private JedisCluster jedisCluster;

    @Mock
    private TransactionHandler transactionHandler;

    @Mock
    private AppProperties appProperties;

    @Mock
    private FileWriterUtil fileWriterUtil;

    @Spy
    @InjectMocks
    private InsertCdr insertCdr;

    @BeforeEach
    void setUp() {
        when(appProperties.getLocation()).thenReturn("/tmp");
        when(appProperties.getApplicationMode()).thenReturn("logs");
    }

    @Test
    void insertOnCdr_logsMode() {
        when(appProperties.getApplicationMode()).thenReturn("logs");
        doNothing().when(insertCdr).writeToLogFile(anyString(), anyInt(), anyLong());

        insertCdr.insertOnCdr("testList", 100, 1000);

        verify(insertCdr).writeToLogFile("testList", 100, 1000);
    }

    @Test
    void insertOnCdr_databaseMode() {
        when(appProperties.getApplicationMode()).thenReturn("database");
        doNothing().when(insertCdr).insertIntoDatabase(anyString(), anyInt(), anyLong());

        insertCdr.insertOnCdr("testList", 100, 1000);

        verify(insertCdr).insertIntoDatabase("testList", 100, 1000);
    }

    @Test
    void insertOnCdr_kafkaMode() {
        when(appProperties.getApplicationMode()).thenReturn("kafka");

        insertCdr.insertOnCdr("testList", 100, 1000);

        verify(insertCdr, never()).writeToLogFile(anyString(), anyInt(), anyLong());
        verify(insertCdr, never()).insertIntoDatabase(anyString(), anyInt(), anyLong());
    }

    @Test
    void insertOnCdr_exceptionHandling() {
        when(appProperties.getApplicationMode()).thenReturn("logs");
        doThrow(new RuntimeException("Test exception")).when(insertCdr).writeToLogFile(anyString(), anyInt(), anyLong());

        insertCdr.insertOnCdr("testList", 100, 1000);

        verify(insertCdr).writeToLogFile("testList", 100, 1000);
    }

    @Test
    void insertIntoDatabase_error() {
        when(insertCdr.getCdrBatches(anyString(), anyInt(), anyLong()))
                .thenReturn(Flux.error(new RuntimeException("Database error")));

        insertCdr.insertIntoDatabase("testList", 100, 1000);

        verify(transactionHandler, never()).cdrPerformed(anyList());
    }

    @Test
    void writeToLogFile_success() {
        String listName = "cdr";
        int batchSize = 5;
        long workForWorker = 5;

       insertCdr.writeToLogFile(listName, batchSize, workForWorker);

        verify(insertCdr).getCdrBatches(listName, batchSize, workForWorker);
    }

    @Test
    void testGetCdrBatches() {
        List<String> cdrList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            UtilsRecords.Cdr cdr = createCdr();
            cdrList.add(Converter.valueAsString(cdr));
        }

        when(jedisCluster.lpop(anyString(), anyInt())).thenReturn(cdrList);

        String listName = "cdr";
        int batchSize = 5;
        long workForWorker = 5;

        Flux<List<UtilsRecords.Cdr>> cdrBatches = insertCdr.getCdrBatches(listName, batchSize, workForWorker);

        StepVerifier.create(cdrBatches)
                .expectNextCount(1)
                .verifyComplete();

        verify(jedisCluster, times(1)).lpop(listName, batchSize);
    }

    @Test
    void getCdrBatches_invalidParameters() {
        Flux<List<UtilsRecords.Cdr>> result = insertCdr.getCdrBatches("testList", -1, 200);
        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();

        result = insertCdr.getCdrBatches("testList", 100, -200);
        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();

        result = insertCdr.getCdrBatches("testList", -1, -200);
        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void getCdrBatches_batchSizeLessThanOrEqualToZero() {
        Flux<List<UtilsRecords.Cdr>> result = insertCdr.getCdrBatches("testList", 0, 100);

        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void writeToFile_accessDeniedException() throws IOException {
        String content = "test content";
        Path filePath = Paths.get("/tmp/cdr.log");

        doThrow(new AccessDeniedException("/tmp/cdr.log"))
                .when(fileWriterUtil).write(eq(filePath), any(byte[].class), eq(StandardOpenOption.CREATE), eq(StandardOpenOption.APPEND));

        Executable writeOperation = () -> insertCdr.writeToFile(content).block();

        assertThrows(RuntimeException.class, writeOperation);
    }

    @Test
    void writeToFile_ioException() throws IOException {
        String content = "test content";
        Path filePath = Paths.get("/tmp/cdr.log");

        doThrow(new IOException())
                .when(fileWriterUtil).write(eq(filePath), any(byte[].class), eq(StandardOpenOption.CREATE), eq(StandardOpenOption.APPEND));

        Executable writeOperation = () -> insertCdr.writeToFile(content).block();
        assertThrows(RuntimeException.class, writeOperation);
    }

    @Test
    void writeToFile_success() throws IOException {
        String content = "test content";
        Path filePath = Paths.get("/tmp/cdr.log");

        doNothing().when(fileWriterUtil).write(eq(filePath), any(byte[].class), eq(StandardOpenOption.CREATE), eq(StandardOpenOption.APPEND));

        insertCdr.writeToFile(content).block();
        verify(fileWriterUtil).write(filePath, content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    private UtilsRecords.Cdr createCdr() {
        return new UtilsRecords.Cdr(
                "1722968788738", "1722968788738", "1722968788735",
                "SMS", "MSG123456789", "SMPP", "NET001", "MO", "SMPP", "NET002", "MT",
                "ROUTE123", "DELIVERED", "0", "No issues", "60", "5", "7-bit",
                "2024-07-24T10:15:30", "1234567890", "1", "1", "0987654321", "1", "1",
                "REMOTE123", "LOCAL123", "SPC001", "SSN001", "GLOBAL001", "SPC002",
                "SSN002", "GLOBAL002", "IMSI001", "NNN001", "SCCP001", "MTSC001",
                "First 20 chars", "EsmClass001", "Udhi001", "Delivery001", "Ref001",
                "1", "1", "0", "Parent001"
        );
    }
}
