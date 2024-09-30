package org.paic.insertdata.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppPropertiesTest {

    @InjectMocks
    private AppProperties appProperties;

    @BeforeEach
    void setUp() throws Exception {
        injectField("redisNodes", Arrays.asList("192.168.100.1:6379", "192.168.100.2:6379", "192.168.100.3:6379"));
        injectField("redisMaxTotal", 20);
        injectField("redisMaxIdle", 20);
        injectField("redisMinIdle", 1);
        injectField("redisBlockWhenExhausted", true);
        injectField("cdrListName", "testCdrList");
        injectField("cdrWorkers", 10);
        injectField("cdrSmRecordsToTake", 100);
        injectField("cdrBatchSize", 20);
        injectField("intervalMillis", 500L);
        injectField("applicationMode", "test");
        injectField("separator", ",");
        injectField("location", "/path/to/file");
        injectField("maxRetries", 3);
    }

    private void injectField(String fieldName, Object value) throws Exception {
        Field field = AppProperties.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(appProperties, value);
    }

    @Test
    void testRedisProperties() {
        assertEquals(Arrays.asList("192.168.100.1:6379", "192.168.100.2:6379", "192.168.100.3:6379"), appProperties.getRedisNodes());
        assertEquals(20, appProperties.getRedisMaxTotal());
        assertEquals(20, appProperties.getRedisMaxIdle());
        assertEquals(1, appProperties.getRedisMinIdle());
        assertTrue(appProperties.isRedisBlockWhenExhausted());
    }

    @Test
    void testCdrProperties() {
        assertEquals("testCdrList", appProperties.getCdrListName());
        assertEquals(10, appProperties.getCdrWorkers());
        assertEquals(100, appProperties.getCdrSmRecordsToTake());
        assertEquals(20, appProperties.getCdrBatchSize());
        assertEquals(500L, appProperties.getIntervalMillis());
    }

    @Test
    void testApplicationProperties() {
        assertEquals("test", appProperties.getApplicationMode());
        assertEquals(",", appProperties.getSeparator());
        assertEquals("/path/to/file", appProperties.getLocation());
        assertEquals(3, appProperties.getMaxRetries());
    }
}
