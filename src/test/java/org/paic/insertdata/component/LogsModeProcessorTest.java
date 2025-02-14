package org.paic.insertdata.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.paic.insertdata.util.AppProperties;
import redis.clients.jedis.JedisCluster;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogsModeProcessorTest {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    JedisCluster jedisCluster;

    @Mock
    AppProperties appProperties;

    @InjectMocks
    LogsModeProcessor logsModeProcessor;

    @Test
    @DisplayName("Process CDR in log when all is ok then verify and read the resulted file and the content")
    void processCdrInLogWhenAllIsOkThenVerifyAndReadTheResultedFileAndTheContent() throws IOException {
        when(appProperties.getCdrListName()).thenReturn("testList");
        when(appProperties.getCdrWorkers()).thenReturn(5);
        when(appProperties.getCdrBatchSize()).thenReturn(1000);
        when(appProperties.getIntervalMillis()).thenReturn(1000L);
        when(appProperties.getSeparator()).thenReturn("|");
        when(jedisCluster.llen("testList")).thenReturn(1L);
        when(jedisCluster.lpop("testList", 1)).thenReturn(Collections.singletonList(objectMapper.writeValueAsString(ObjectsCreator.getDefaultCdr())));
        assertDoesNotThrow(() -> logsModeProcessor.processCdrInLogsFile());
        try (ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor()) {
            scheduledExecutorService.schedule(() -> {
            }, 2, java.util.concurrent.TimeUnit.SECONDS);
        }

        File file = new File("./target/generated-sources/logs/cdr.log");
        assertTrue(file.exists());

        String content = FileUtils.readFileToString(file, "UTF-8");
        assertTrue(content.contains("200|8|55566768|730169999999212|55566768||22220|First 20 chars|0||0|||||1734454582187-9026385306105"));
    }
}