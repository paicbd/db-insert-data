package org.paic.insertdata.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paicbd.smsc.dto.UtilsRecords;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.paic.insertdata.component.DatabaseModeProcessor;
import org.paic.insertdata.component.LogsModeProcessor;
import org.paic.insertdata.component.ObjectsCreator;
import reactor.core.publisher.Flux;
import redis.clients.jedis.JedisCluster;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CommonUtilsTest {

    @Test
    @DisplayName("Should create string cdr when param is cdr then cast correctly")
    void createStringCdrWhenParamIsCdrThenCastOk() {
        UtilsRecords.Cdr cdr = ObjectsCreator.getDefaultCdr();
        String result = CommonUtils.createStringCdr(cdr, "|");
        assertTrue(result.contains("MESSAGE|1734454582187-9026385306105|HTTP|3|SP|SS7|2|GW|1||SENT||1773|1775|0|60|1322888089|1|1|0987654321|1|1||73|0|8|22220|200|8|55566768|730169999999212|55566768||22220|First 20 chars|0||0|||||1734454582187-9026385306105"));
    }

    @Test
    @DisplayName("Process cdr when receive logs mode then execute correctly")
    void processCdrWhenReceiveDatabaseModeMethodThenExecuteCorrectly() {
        AppProperties appProperties = mock(AppProperties.class);
        JedisCluster jedisCluster = mock(JedisCluster.class);
        DatabaseModeProcessor databaseModeProcessor = mock(DatabaseModeProcessor.class);

        when(appProperties.getCdrListName()).thenReturn("cdrListName");
        when(appProperties.getCdrWorkers()).thenReturn(1);
        when(appProperties.getCdrBatchSize()).thenReturn(1);
        when(appProperties.getIntervalMillis()).thenReturn(1000L);
        when(jedisCluster.llen("cdrListName")).thenReturn(1L);

        CommonUtils.processCdr("Database", appProperties, jedisCluster, databaseModeProcessor::insertIntoDatabase);
        verify(databaseModeProcessor).insertIntoDatabase("cdrListName", 1, 1);
    }

    @Test
    @DisplayName("Process cdr when receive logs mode but workers is invalid then not execute the method")
    void processCdrWhenWorkersIsInvalidNumberThenNotExecuteTheMethod() {
        AppProperties appProperties = mock(AppProperties.class);
        JedisCluster jedisCluster = mock(JedisCluster.class);
        LogsModeProcessor logsModeProcessor = mock(LogsModeProcessor.class);

        when(appProperties.getCdrWorkers()).thenReturn(0);

        CommonUtils.processCdr("Logs", appProperties, jedisCluster, logsModeProcessor::writeToLogFiles);
        verify(logsModeProcessor, never()).writeToLogFiles(anyString(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("Process cdr when data is not found in redis then not execute")
    void processCdrWhenDataIsNotFoundInRedisThenNotExecute() {
        AppProperties appProperties = mock(AppProperties.class);
        JedisCluster jedisCluster = mock(JedisCluster.class);
        LogsModeProcessor logsModeProcessor = mock(LogsModeProcessor.class);

        when(appProperties.getCdrWorkers()).thenReturn(1);
        when(appProperties.getCdrBatchSize()).thenReturn(1);
        when(appProperties.getIntervalMillis()).thenReturn(1000L);
        when(jedisCluster.llen("cdrListName")).thenReturn(0L);

        CommonUtils.processCdr("Logs", appProperties, jedisCluster, logsModeProcessor::writeToLogFiles);
        verify(logsModeProcessor, never()).writeToLogFiles(anyString(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("Get cdr batches when data is found in redis then execute successfully and return the correct flux")
    void getCdrBatchesWhenGetDataCorrectlyFromRedisThenExecuteSuccessfully() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JedisCluster jedisCluster = mock(JedisCluster.class);
        String cdrString = objectMapper.writeValueAsString(ObjectsCreator.getDefaultCdr());

        when(jedisCluster.lpop("cdrListName", 1)).thenReturn(List.of(cdrString));
        when(jedisCluster.lpop("cdrListName", 2)).thenReturn(List.of(cdrString, cdrString));
        when(jedisCluster.lpop("cdrListName", 3)).thenReturn(List.of(cdrString, cdrString, cdrString));

        Flux<List<UtilsRecords.Cdr>> result1 = CommonUtils.getCdrBatches(jedisCluster, "cdrListName", 1, 1);
        Flux<List<UtilsRecords.Cdr>> result2 = CommonUtils.getCdrBatches(jedisCluster, "cdrListName", 2, 1);
        Flux<List<UtilsRecords.Cdr>> result3 = CommonUtils.getCdrBatches(jedisCluster, "cdrListName", 3, 1);

        assertEquals(1, Objects.requireNonNull(result1.blockFirst()).size());
        assertEquals(2, Objects.requireNonNull(result2.blockFirst()).size());
        assertEquals(3, Objects.requireNonNull(result3.blockFirst()).size());
    }
}
