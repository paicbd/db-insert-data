package org.paic.insertdata.config;

import com.paicbd.smsc.utils.Generated;
import lombok.RequiredArgsConstructor;
import org.paic.insertdata.util.AppProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Generated
@Configuration
@RequiredArgsConstructor
public class BeansDefinition {
    private final AppProperties appProperties;

    @Bean
    @Conditional(DatabaseCondition.class)
    public DataSource dataSource() {
        return DataSourceBuilder.create()
                .url(appProperties.getDatasourceUrl())
                .username(appProperties.getDatasourceUsername())
                .password(appProperties.getDatasourcePassword())
                .driverClassName(appProperties.getDatasourceDriverClassName())
                .build();
    }
}
