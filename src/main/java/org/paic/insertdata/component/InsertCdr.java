package org.paic.insertdata.component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.paicbd.smsc.dto.UtilsRecords;
import com.paicbd.smsc.utils.Converter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.paic.insertdata.util.TransactionHandler;
import org.paic.insertdata.util.DataConverter;
import org.paic.insertdata.util.AppProperties;
import org.paic.insertdata.util.FileWriterUtil;
import org.paic.insertdata.exception.FileOperationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import redis.clients.jedis.JedisCluster;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Class for inserting cdr_detail into a database asynchronously.
 */
@Component
@RequiredArgsConstructor
@Slf4j(topic = "insertCdr")
public class InsertCdr {
    private final JedisCluster jedisCluster;
    private final TransactionHandler transactionHandler;
    private final AppProperties appProperties;
    private final FileWriterUtil fileWriterUtil;

    /**
     * Inserts Cdr into the database asynchronously.
     *
     * @param listName      the name of the list containing CDR in Redis
     * @param batchSize     the size of each batch of CDR to process
     * @param workForWorker the total number of CDR to process for each worker
     */
    @Async
    public void insertOnCdr(String listName, int batchSize, long workForWorker) {
        try {
            log.debug("Method insertOnCdr is called");

            switch (appProperties.getApplicationMode().toLowerCase()) {
                case "logs" -> writeToLogFile(listName, batchSize, workForWorker);
                case "database" -> insertIntoDatabase(listName, batchSize, workForWorker);
                case "kafka" -> log.info("Kafka mode is not implemented");
                default -> throw new IllegalArgumentException("Undefined mode");
            }
        } catch (Exception e) {
            log.error("Error while processing cdr_detail", e);
        }
    }

    /**
     * Writes CDR to a log file, creating a file for each day and appending CDR entries.
     *
     * @param listName      the name of the list containing CDR in Redis
     * @param batchSize     the size of each batch of CDR to process
     * @param workForWorker the total number of CDR to process for each worker
     */
    public void writeToLogFile(String listName, int batchSize, long workForWorker) {
        var response = getCdrBatches(listName, batchSize, workForWorker);
        response.publishOn(Schedulers.boundedElastic())
                .flatMap(list -> Flux.fromIterable(list)
                        .mapNotNull(Converter::valueAsString)
                        .map(cdrString -> Converter.stringToObject(cdrString, new TypeReference<UtilsRecords.Cdr>() {}))
                        .map(cdr -> DataConverter.createStringCdr(cdr, appProperties.getSeparator()))
                        .collectList()
                        .map(cdrStrings -> String.join(System.lineSeparator(), cdrStrings) + System.lineSeparator())
                        .flatMap(this::writeToFile)
                )
                .doOnComplete(() -> log.warn("cdr data logged successfully"))
                .subscribe();
    }

    /**
     * Inserts CDR into the database.
     *
     * @param listName      the name of the list containing CDR in Redis
     * @param batchSize     the size of each batch of CDR to process
     * @param workForWorker the total number of CDR to process for each worker
     */
    public void insertIntoDatabase(String listName, int batchSize, long workForWorker) {
        var response = getCdrBatches(listName, batchSize, workForWorker);
        response.publishOn(Schedulers.boundedElastic())
                .flatMap(list -> Mono.fromCallable(() -> {
                            log.debug("Inserting {} cdr into database.", list.size());
                            transactionHandler.cdrPerformed(list);
                            return null;
                        }).subscribeOn(Schedulers.boundedElastic())
                )
                .doOnComplete(() -> log.warn("cdr data inserted successfully"))
                .subscribe();
    }

    /**
     * Retrieves batches of CDR from Redis.
     *
     * @param listName        the name of the list containing cdr_detail in Redis
     * @param batchSize       the size of each batch of cdr to retrieve
     * @param workForWorker   the total number of cdr to retrieve for each worker
     * @return a {@link Flux} of lists of cdr
     */
    public Flux<List<UtilsRecords.Cdr>> getCdrBatches(String listName, int batchSize, long workForWorker) {
        if (batchSize <= 0 || workForWorker <= 0) {
            return Flux.empty();
        }

        log.warn("Worker is getting cdr from redis");
        int batchesQuantity = (int) workForWorker / batchSize;
        int lastBatchSize = (int) workForWorker % batchSize;

        Flux<List<UtilsRecords.Cdr>> result = Flux.range(0, batchesQuantity)
                .map(i -> batchSize)
                .concatWith(lastBatchSize > 0 ? Flux.just(lastBatchSize) : Flux.empty())
                .flatMap(size -> {
                    List<String> cdrList = jedisCluster.lpop(listName, size);
                    if (cdrList != null) {
                        return Flux.fromIterable(cdrList)
                                .mapNotNull(cdr -> Converter.stringToObject(cdr, new TypeReference<UtilsRecords.Cdr>() {
                                }))
                                .filter(Objects::nonNull)
                                .sort(Comparator.comparing(UtilsRecords.Cdr::recordDate))
                                .collectList();
                    } else {
                        return Flux.empty();
                    }
                });

        return result.publishOn(Schedulers.parallel())
                .doOnComplete(() -> log.info("The cdr batches has been completed"));
    }

    public Mono<Void> writeToFile(String content) {
        return Mono.fromRunnable(() -> {
                    String fileName = "";
                    try {
                        LocalDate currentDate = LocalDate.now();
                        String dateString = currentDate.format(DateTimeFormatter.ISO_DATE);
                        String directory = appProperties.getLocation();

                        fileName = directory + "/cdr.log";

                        LocalDate nextDay = currentDate.plusDays(1);
                        if (!currentDate.equals(nextDay.minusDays(1))) {
                            fileName = directory + "/cdr-" + dateString + ".log";
                        }

                        Path dirPath = Paths.get(directory);
                        Path filePath = Paths.get(fileName);

                        if (fileWriterUtil.notExists(dirPath)) {
                            fileWriterUtil.createDirectories(dirPath);
                        }

                        fileWriterUtil.write(filePath, content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                    } catch (AccessDeniedException e) {
                        log.error("Access denied to file '{}': {}", fileName, e.getMessage(), e);
                        throw new FileOperationException("Access denied to file: " + fileName, e);
                    } catch (IOException e) {
                        log.error("Error writing to file '{}': {}", fileName, e.getMessage(), e);
                        throw new FileOperationException("Error writing to file: " + fileName, e);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnError(e -> log.error("Error writing to file", e)).then();
    }
}
