package org.paic.insertdata.config;

import com.paicbd.smsc.utils.Generated;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisCluster;
import com.paicbd.smsc.dto.UtilsRecords;
import com.paicbd.smsc.utils.Converter;
import org.paic.insertdata.util.AppProperties;

import javax.sql.DataSource;

@Generated
@Configuration
@RequiredArgsConstructor
public class BeansDefinition {
    private final AppProperties appProperties;

    @Bean
    public JedisCluster jedisCluster() {
        return Converter.paramsToJedisCluster(
                new UtilsRecords.JedisConfigParams(appProperties.getRedisNodes(), appProperties.getRedisMaxTotal(),
                        appProperties.getRedisMaxIdle(), appProperties.getRedisMinIdle(),
                        appProperties.isRedisBlockWhenExhausted(), appProperties.getRedisConnectionTimeout(),
                        appProperties.getRedisSoTimeout(), appProperties.getRedisMaxAttempts(),
                        appProperties.getRedisUser(), appProperties.getRedisPassword())
        );
    }

    @Bean
    @ConditionalOnProperty(name = "application.mode", havingValue = "database")
    public DataSource dataSource() {
        return DataSourceBuilder.create()
                .url(appProperties.getDatasourceUrl())
                .username(appProperties.getDatasourceUsername())
                .password(appProperties.getDatasourcePassword())
                .driverClassName(appProperties.getDatasourceDriverClassName())
                .build();
    }
}
