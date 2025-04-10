package org.paic.insertdata.util;

import com.paicbd.smsc.dto.UtilsRecords;
import com.paicbd.smsc.utils.DataFetcher;
import com.paicbd.smsc.utils.Generated;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import redis.clients.jedis.JedisCluster;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.paicbd.smsc.utils.Converter.stringToObject;

@Slf4j
public class CommonUtils {
    private static final ZoneId ZONE_ID = ZoneId.systemDefault();

    @Generated
    private CommonUtils() {
        throw new IllegalStateException("Utility Class");
    }

    public static String createStringCdr(UtilsRecords.Cdr cdr, String separator) {
        return String.join(separator,
                convertMillisecondsToDateTimeString(cdr.recordDate()),
                convertMillisecondsToDateTimeString(cdr.submitDate()),
                convertMillisecondsToDateTimeString(cdr.deliveryDate()),
                cdr.messageType(),
                cdr.messageId(),
                cdr.originationProtocol(),
                cdr.originationNetworkId(),
                cdr.originationType(),
                cdr.destinationProtocol(),
                cdr.destinationNetworkId(),
                cdr.destinationType(),
                cdr.routingId(),
                cdr.status(),
                cdr.statusCode(),
                cdr.comment(),
                cdr.dialogDuration(),
                cdr.processingTime(),
                cdr.dataCoding(),
                cdr.validityPeriod(),
                cdr.addrSrcDigits(),
                cdr.addrSrcTon(),
                cdr.addrSrcNpi(),
                cdr.addrDstDigits(),
                cdr.addrDstTon(),
                cdr.addrDstNpi(),
                cdr.remoteDialogId(),
                cdr.localDialogId(),
                cdr.localSpc(),
                cdr.localSsn(),
                cdr.localGlobalTitleDigits(),
                cdr.remoteSpc(),
                cdr.remoteSsn(),
                cdr.remoteGlobalTitleDigits(),
                cdr.imsi(),
                cdr.nnnDigits(),
                cdr.originatorSccpAddress(),
                cdr.mtServiceCenterAddress(),
                cdr.first20CharacterOfSms(),
                cdr.esmClass(),
                cdr.udhi(),
                cdr.registeredDelivery(),
                cdr.msgReferenceNumber(),
                cdr.totalSegment(),
                cdr.segmentSequence(),
                cdr.retryNumber(),
                cdr.parentId()
        );
    }

    /**
     * Converts an ISO_DATE formatted date string to a LocalDate object.
     *
     * @param milliseconds the date string in ISO_DATE format
     * @return the corresponding LocalDate object, or null if conversion fails
     */
    private static String convertMillisecondsToDateTimeString(String milliseconds) {
        try {
            long timestamp = Long.parseLong(milliseconds);
            Instant instant = Instant.ofEpochMilli(timestamp);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS").withZone(ZONE_ID);
            return formatter.format(instant);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static void processCdr(
            String mode, AppProperties appProperties, JedisCluster jedisCluster, TriConsumer<String, Integer, Integer> method) {
        String configListName = appProperties.getCdrListName();
        Map.Entry<Integer, Integer> workersAndBatch = CommonUtils.prepareCdrProcessing(
                appProperties, jedisCluster, configListName, appProperties.getCdrWorkers(), appProperties.getCdrBatchSize());

        if (Objects.nonNull(workersAndBatch)) {
            int workers = workersAndBatch.getKey();
            int batchSize = workersAndBatch.getValue();

            log.info("{} mode. Processing CDR with workers: {}, batchSize: {}", mode, workers, batchSize);
            method.accept(configListName, batchSize, workers);
        }
    }

    private static Map.Entry<Integer, Integer> prepareCdrProcessing(
            AppProperties appProperties, JedisCluster jedisCluster, String configListName, long configWorkers, int configBatchSize) {
        if (configWorkers < 1) {
            log.error("Invalid number of workers: {}", configWorkers);
            return null;
        }

        Map.Entry<Integer, Integer> workersAndBatch =
                DataFetcher.calculateWorkersAndBatchSize(
                        jedisCluster,
                        configBatchSize,
                        (int) appProperties.getIntervalMillis(),
                        (int) configWorkers,
                        configListName);

        int workers = workersAndBatch.getKey();
        if (workers == 0) {
            log.debug("No CDR to process");
            return null;
        }

        return workersAndBatch;
    }

    public static Flux<List<UtilsRecords.Cdr>> getCdrBatches(
            JedisCluster jedisCluster, String listName, int batchSize, int workers) {
        return Flux.range(0, workers)
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(worker -> {
                    List<String> batch = jedisCluster.lpop(listName, batchSize);
                    return Flux.fromIterable(batch)
                            .parallel()
                            .runOn(Schedulers.parallel())
                            .map(msgRaw -> stringToObject(msgRaw, UtilsRecords.Cdr.class))
                            .filter(Objects::nonNull)
                            .sequential()
                            .collectSortedList(Comparator.comparing(
                                    UtilsRecords.Cdr::recordDate,
                                    Comparator.nullsLast(Comparator.naturalOrder())
                            ));
                });
    }
}
