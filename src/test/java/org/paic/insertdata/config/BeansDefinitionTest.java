package org.paic.insertdata.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.paic.insertdata.util.AppProperties;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BeansDefinitionTest {

    @Mock
    private AppProperties appProperties;

    @InjectMocks
    private BeansDefinition beansDefinition;

    @Test
    void testJedisClusterCreation() {
        when(appProperties.getRedisNodes()).thenReturn(List.of("localhost:6379", "localhost:6380"));
        when(appProperties.getRedisMaxTotal()).thenReturn(10);
        when(appProperties.getRedisMinIdle()).thenReturn(1);
        when(appProperties.getRedisMaxIdle()).thenReturn(5);
        when(appProperties.isRedisBlockWhenExhausted()).thenReturn(true);
        assertNull(beansDefinition.jedisCluster());
    }

}