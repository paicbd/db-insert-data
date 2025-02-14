package org.paic.insertdata.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paicbd.smsc.dto.UtilsRecords;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.paic.insertdata.util.AppProperties;
import org.springframework.dao.DataAccessException;
import redis.clients.jedis.JedisCluster;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatabaseModeProcessorTest {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private JedisCluster jedisCluster;

    @Mock
    private AppProperties appProperties;

    @Mock
    private BulkInserter bulkInserter;

    @InjectMocks
    private DatabaseModeProcessor databaseModeProcessor;

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("Process CDR in database when data is ok then execute bulk inserter")
    void processCdrInDbWhenDataIsOkThenExecuteBulkInserter() throws IOException {
        when(appProperties.getCdrListName()).thenReturn("testList");
        when(appProperties.getCdrWorkers()).thenReturn(5);
        when(appProperties.getCdrBatchSize()).thenReturn(1000);
        when(appProperties.getIntervalMillis()).thenReturn(1000L);
        when(jedisCluster.llen("testList")).thenReturn(1L);
        when(jedisCluster.lpop("testList", 1)).thenReturn(Collections.singletonList(objectMapper.writeValueAsString(ObjectsCreator.getDefaultCdr())));
        when(appProperties.getMaxRetries()).thenReturn(3);
        assertDoesNotThrow(() -> databaseModeProcessor.processCdrInDatabase());
        try (ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor()) {
            scheduledExecutorService.schedule(() -> {
            }, 1, java.util.concurrent.TimeUnit.SECONDS);
        }

        ArgumentCaptor<List<UtilsRecords.Cdr>> captor = ArgumentCaptor.forClass(List.class);
        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> verify(bulkInserter, atLeast(1)).saveCdrBulk(captor.capture()));

        assertNotNull(captor.getValue());
        List<UtilsRecords.Cdr> cdrList = captor.getValue();
        assertNotNull(cdrList);
        assertEquals(1, cdrList.size());

        UtilsRecords.Cdr cdr = cdrList.getFirst();
        assertNotNull(cdr);
    }

    @Test
    @DisplayName("Process CDR in database when retries is zero then not execute bulk inserter")
    void processCdrInDbWhenRetriesIsZeroThenNotExecuteBulkInserter() throws IOException {
        when(appProperties.getCdrListName()).thenReturn("testList");
        when(appProperties.getCdrWorkers()).thenReturn(5);
        when(appProperties.getCdrBatchSize()).thenReturn(1000);
        when(appProperties.getIntervalMillis()).thenReturn(1000L);
        when(jedisCluster.llen("testList")).thenReturn(1L);
        when(jedisCluster.lpop("testList", 1)).thenReturn(Collections.singletonList(objectMapper.writeValueAsString(ObjectsCreator.getDefaultCdr())));
        when(appProperties.getMaxRetries()).thenReturn(0);
        assertDoesNotThrow(() -> databaseModeProcessor.processCdrInDatabase());
        try (ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor()) {
            scheduledExecutorService.schedule(() -> {
            }, 1, java.util.concurrent.TimeUnit.SECONDS);
        }

        verify(bulkInserter, never()).saveCdrBulk(anyList());
    }

    @Test
    @DisplayName("Process CDR in database when bulk inserter throws exception then retry the max retries number")
    void processCdrInDbWhenBulkInserterThrowsExceptionThenRetryTheMaxRetriesNumber() throws IOException {
        when(appProperties.getCdrListName()).thenReturn("testList");
        when(appProperties.getCdrWorkers()).thenReturn(5);
        when(appProperties.getCdrBatchSize()).thenReturn(1000);
        when(appProperties.getIntervalMillis()).thenReturn(1000L);
        when(jedisCluster.llen("testList")).thenReturn(1L);
        when(jedisCluster.lpop("testList", 1)).thenReturn(Collections.singletonList(objectMapper.writeValueAsString(ObjectsCreator.getDefaultCdr())));
        when(appProperties.getMaxRetries()).thenReturn(3);
        doThrow(new DataAccessException("Error") {
        }).when(bulkInserter).saveCdrBulk(anyList());

        databaseModeProcessor.processCdrInDatabase();
        try (ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor()) {
            scheduledExecutorService.schedule(() -> {
            }, 1, java.util.concurrent.TimeUnit.SECONDS);
        }

        verify(bulkInserter, times(3)).saveCdrBulk(anyList());
    }
}