package org.paic.insertdata.component;

import com.paicbd.smsc.dto.UtilsRecords;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.paic.insertdata.util.AppProperties;
import org.paic.insertdata.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import redis.clients.jedis.JedisCluster;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "application.mode", havingValue = "logs")
public class LogsModeProcessor {
    private static final Logger cdrLogger = LoggerFactory.getLogger("cdrLogger");
    private final JedisCluster jedisCluster;
    private final AppProperties appProperties;

    @Async
    @Scheduled(fixedRateString = "${configuration.interval-millis}")
    public void processCdrInLogsFile() {
        CommonUtils.processCdr("Logs", appProperties, jedisCluster, this::writeToLogFiles);
    }

    public void writeToLogFiles(String listName, int batchSize, int workers) {
        log.debug("Writing cdr to log file. listName: {}, batchSize: {}, workers: {}", listName, batchSize, workers);
        Flux<List<UtilsRecords.Cdr>> cdrFlux = CommonUtils.getCdrBatches(jedisCluster, listName, batchSize, workers);

        cdrFlux.flatMapIterable(batch -> batch)
                .map(cdr -> CommonUtils.createStringCdr(cdr, appProperties.getSeparator()))
                .buffer(batchSize)
                .map(cdrStrings -> String.join(System.lineSeparator(), cdrStrings))
                .doOnNext(cdrLogger::info)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }
}
