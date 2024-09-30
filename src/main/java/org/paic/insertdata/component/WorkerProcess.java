package org.paic.insertdata.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.paic.insertdata.util.AppProperties;
import org.paic.insertdata.exception.FileOperationException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import redis.clients.jedis.JedisCluster;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkerProcess {

    private static final Set<String> MODES = Set.of("database", "logs", "kafka");

    private final JedisCluster jedisCluster;
    private final InsertCdr insertCdr;
    private final AppProperties properties;

    private ExecutorService cdrExecutorService;

    @PostConstruct
    public void init() {
        log.info("Initializing workers");
        int numberOfWorkers = properties.getCdrWorkers();
        if (numberOfWorkers <= 0) {
            log.error("Invalid number of workers: {}", numberOfWorkers);
            throw new FileOperationException("Invalid number of workers: " + numberOfWorkers, null);
        }
        cdrExecutorService = Executors.newFixedThreadPool(numberOfWorkers);
        this.startCdrWorker();
    }

    public void startCdrWorker() {
        log.debug("Starting CDR worker with interval: {} milliseconds", properties.getIntervalMillis());

        Flux.interval(Duration.ofMillis(properties.getIntervalMillis()), Schedulers.boundedElastic())
                .flatMap(ignored -> Mono.fromFuture(this::cdrWorker))
                .subscribe();
    }

    public CompletableFuture<Void> cdrWorker() {
        if (cdrExecutorService == null) {
            throw new IllegalStateException("cdrExecutorService is not initialized");
        }

        return CompletableFuture.runAsync(() -> {
            try {
                long length = getCdrLength();
                if (shouldProcessCdr(length)) {
                    long workForWorker = calculateWorkForWorker(length);
                    processCdr(workForWorker);
                }
            } catch (Exception e) {
                log.error("Error processing CDR", e);
            }
        }, cdrExecutorService);
    }

    public long getCdrLength() {
        log.debug("Getting CDR from Redis");
        return Math.min(jedisCluster.llen(properties.getCdrListName()), properties.getCdrSmRecordsToTake());
    }

    public boolean shouldProcessCdr(long length) {
        if (length == 0 || length < properties.getCdrWorkers()) {
            log.debug("No CDR to process");
            return false;
        }
        return true;
    }

    public long calculateWorkForWorker(long length) {
        return length / properties.getCdrWorkers();
    }

    public void processCdr(long workForWorker) {
        CompletableFuture<?>[] futures = new CompletableFuture[properties.getCdrWorkers()];

        for (int i = 0; i < properties.getCdrWorkers(); i++) {
            int workerIndex = i;
            futures[workerIndex] = CompletableFuture.runAsync(() -> {
                log.debug("Processing CDR batch {}", workerIndex + 1);
                insertCdr.insertOnCdr(properties.getCdrListName(), properties.getCdrBatchSize(), workForWorker);
            }, cdrExecutorService);
        }

        CompletableFuture.allOf(futures).join();
    }

    @PreDestroy
    public void shutdown() {
        if (cdrExecutorService != null && !cdrExecutorService.isShutdown()) {
            log.info("Shutting down executor service");
            cdrExecutorService.shutdown();
        }
    }
}
