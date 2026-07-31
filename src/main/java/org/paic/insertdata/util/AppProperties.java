package org.paic.insertdata.util;

import com.paicbd.smsc.utils.Generated;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Generated
@Component
public class AppProperties {

    @Value("${configuration.cdr-workers}")
    private int cdrWorkers;

    @Value("${configuration.cdr-batch-size}")
    private int cdrBatchSize;

    @Value("${configuration.interval-millis}")
    private long intervalMillis;

    @Value("${application.mode:logs}")
    private String applicationMode;

    @Value("${application.cdr.separator}")
    private String separator;

    @Value("${jdbc.max-retries:5}")
    private int maxRetries;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String datasourceUsername;

    @Value("${spring.datasource.password}")
    private String datasourcePassword;

    @Value("${spring.datasource.driver-class-name}")
    private String datasourceDriverClassName;

    @Value("${application.cdr.message-length}")
    private int cdrMessageLength;

    @Value("${spring.kafka.bootstrap-servers}")
    private String kafkaBootstrapServers;

    @Value("${spring.kafka.listener.concurrency}")
    private int kafkaListenerConcurrency;
}