package org.paic.insertdata.component;

import com.paicbd.smsc.dto.UtilsRecords;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.paic.insertdata.util.AppProperties;
import org.paic.insertdata.util.CommonUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import redis.clients.jedis.JedisCluster;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "application.mode", havingValue = "database")
public class DatabaseModeProcessor {
    private final JedisCluster jedisCluster;
    private final AppProperties appProperties;
    private final BulkInserter bulkInserter;

    @Async
    @Scheduled(fixedRateString = "${configuration.interval-millis}")
    public void processCdrInDatabase() {
        CommonUtils.processCdr("Database", appProperties, jedisCluster, this::insertIntoDatabase);
    }

    public void insertIntoDatabase(String listName, int batchSize, int workers) {
        log.debug("Inserting cdr to database. listName: {}, batchSize: {}, workers: {}", listName, batchSize, workers);
        Flux<List<UtilsRecords.Cdr>> cdrFlux = CommonUtils.getCdrBatches(jedisCluster, listName, batchSize, workers);

        cdrFlux.publishOn(Schedulers.boundedElastic())
                .flatMap(list -> Mono.fromRunnable(() -> cdrPerformed(list))
                        .subscribeOn(Schedulers.boundedElastic()))
                .doOnComplete(() -> log.warn("CDR data inserted successfully into the database"))
                .subscribe();
    }

    private void cdrPerformed(List<UtilsRecords.Cdr> list) {
        int retries = 0;
        boolean success = false;
        while (retries < appProperties.getMaxRetries() && !success) {
            try {
                bulkInserter.saveCdrBulk(list);
                success = true;
            } catch (DataAccessException e) {
                log.error("Error while saving cdr to database, retry number: {}", retries, e);
                retries++;
            }
        }
    }
}
