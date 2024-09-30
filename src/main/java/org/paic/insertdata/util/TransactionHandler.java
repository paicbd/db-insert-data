package org.paic.insertdata.util;

import com.paicbd.smsc.dto.UtilsRecords;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * The {@link TransactionHandler} class is responsible for saving data related to submit_sm, deliver_sm, and sm_response to the database.
 */
@Slf4j(topic = "transactionHandler")
@Repository
@RequiredArgsConstructor
public class TransactionHandler {
    private final BulkInserter bulkInserter;
    private final AppProperties properties;

    @Transactional
    public void cdrPerformed(List<UtilsRecords.Cdr> list) {
        int retries = 0;
        boolean success = false;
        while (retries < properties.getMaxRetries() && !success) {
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
