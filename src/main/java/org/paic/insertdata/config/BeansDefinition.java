package org.paic.insertdata.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisCluster;
import com.paicbd.smsc.dto.UtilsRecords;
import com.paicbd.smsc.utils.Converter;
import org.paic.insertdata.util.AppProperties;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class BeansDefinition {
    private final AppProperties appProperties;

    @Bean
    public JedisCluster jedisCluster() {
        return Converter.paramsToJedisCluster(getJedisClusterParams(appProperties.getRedisNodes(), appProperties.getRedisMaxTotal(),
                appProperties.getRedisMinIdle(), appProperties.getRedisMaxIdle(), appProperties.isRedisBlockWhenExhausted()));
    }

    private UtilsRecords.JedisConfigParams getJedisClusterParams(List<String> nodes, int maxTotal, int minIdle, int maxIdle, boolean blockWhenExhausted) {
        return new UtilsRecords.JedisConfigParams(nodes, maxTotal, minIdle, maxIdle, blockWhenExhausted);
    }
}
