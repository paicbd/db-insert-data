package org.paic.insertdata.component;

import com.paicbd.smsc.dto.UtilsRecords;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.paic.insertdata.config.DatabaseCondition;
import org.paic.insertdata.util.AppProperties;
import org.springframework.context.annotation.Conditional;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Conditional(DatabaseCondition.class)
public class DatabaseComponent {

    private final AppProperties appProperties;
    private final BulkInserter bulkInserter;

    public void storeInDatabase(Flux<List<UtilsRecords.Cdr>> cdrFlux) {
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
