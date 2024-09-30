package org.paic.insertdata.component;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.paic.insertdata.util.AppProperties;
import redis.clients.jedis.JedisCluster;

import java.lang.reflect.Field;
import java.util.concurrent.ExecutorService;

import org.paic.insertdata.exception.FileOperationException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyLong;

@Slf4j
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkerProcessTest {

    @Mock
    private JedisCluster jedisCluster;

    @Mock
    private InsertCdr insertCdr;

    @Mock
    private AppProperties properties;

    @InjectMocks
    private WorkerProcess workerProcess;

    @BeforeEach
    void setUp() {
        when(properties.getCdrWorkers()).thenReturn(3);
        when(properties.getCdrListName()).thenReturn("testList");
        when(properties.getCdrBatchSize()).thenReturn(100);
        when(properties.getCdrSmRecordsToTake()).thenReturn(1000);
        when(jedisCluster.llen("testList")).thenReturn(1000L);

        doAnswer(invocation -> {
            log.debug("Simulating insertOnCdr completion");
            return null;
        }).when(insertCdr).insertOnCdr(anyString(), anyInt(), anyLong());

        workerProcess.init();
    }

    @Test
    void testInit() throws NoSuchFieldException, IllegalAccessException {
        when(properties.getCdrWorkers()).thenReturn(5);

        workerProcess.init();
        Field executorServiceField = WorkerProcess.class.getDeclaredField("cdrExecutorService");
        executorServiceField.setAccessible(true);
        ExecutorService cdrExecutorService = (ExecutorService) executorServiceField.get(workerProcess);
        assertNotNull(cdrExecutorService, "Executor service should not be null");
    }

    @Test
    void testInit_InvalidNumberOfWorkers() {
        when(properties.getCdrWorkers()).thenReturn(0);

        FileOperationException thrownException = assertThrows(FileOperationException.class, () -> {
            workerProcess.init();
        });

        assertEquals("Invalid number of workers: 0", thrownException.getMessage());
    }

    @Test
    void testCdrWorker_cdrExecutorServiceNotInitialized() throws NoSuchFieldException, IllegalAccessException {
        Field executorServiceField = WorkerProcess.class.getDeclaredField("cdrExecutorService");
        executorServiceField.setAccessible(true);
        executorServiceField.set(workerProcess, null);

        assertThrows(IllegalStateException.class, () -> workerProcess.cdrWorker());
    }

    @Test
    void testGetCdrLength() {
        long length = workerProcess.getCdrLength();
        assertEquals(1000, length);
    }

    @Test
    void testShouldProcessCdr() {
        assertFalse(workerProcess.shouldProcessCdr(0));
        assertFalse(workerProcess.shouldProcessCdr(2));

        assertTrue(workerProcess.shouldProcessCdr(1000));
    }

    @Test
    void testCalculateWorkForWorker() {
        long work = workerProcess.calculateWorkForWorker(1000);
        assertEquals(333, work);
    }

    @Test
    void testShutdown() {
        assertDoesNotThrow(() -> workerProcess.shutdown());
    }
}
