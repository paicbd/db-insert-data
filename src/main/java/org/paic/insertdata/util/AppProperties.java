package org.paic.insertdata.util;


import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Component
public class AppProperties {

    @Value("#{'${redis.cluster.nodes}'.split(',')}")
    private List<String> redisNodes;

    @Value("${redis.threadPool.maxTotal:20}")
    private int redisMaxTotal;

    @Value("${redis.threadPool.maxIdle:20}")
    private int redisMaxIdle;

    @Value("${redis.threadPool.minIdle:1}")
    private int redisMinIdle;

    @Value("${redis.threadPool.blockWhenExhausted:true}")
    private boolean redisBlockWhenExhausted;

    @Value("${configuration.cdr}")
    private String cdrListName;

    @Value("${configuration.cdr-workers}")
    private int cdrWorkers;

    @Value("${configuration.cdr-records-take}")
    private int cdrSmRecordsToTake;

    @Value("${configuration.cdr-batch-size}")
    private int cdrBatchSize;

    @Value("${configuration.interval-millis}")
    private long intervalMillis;

    @Value("${application.mode:logs}")
    private String applicationMode;

    @Value("${application.cdr.separator}")
    private String separator;

    @Value("${application.cdr.location}")
    private String location;

    @Value("${jdbc.max-retries:5}")
    private int maxRetries;
}