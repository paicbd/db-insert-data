package org.paic.insertdata.util;

import com.paicbd.smsc.dto.UtilsRecords;
import org.springframework.dao.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TransactionHandlerTest {

    @Mock
    private BulkInserter bulkInserter;

    @Mock
    private AppProperties properties;

    @InjectMocks
    private TransactionHandler transactionHandler;

    @BeforeEach
    void setUp() {
        when(properties.getMaxRetries()).thenReturn(3);
    }

    @Test
    void cdrPerformed_successfulSave() {
        List<UtilsRecords.Cdr> cdrList = Collections.singletonList(createCdr() );

        transactionHandler.cdrPerformed(cdrList);

        verify(bulkInserter, times(1)).saveCdrBulk(cdrList);
        verifyNoMoreInteractions(bulkInserter);
    }

    @Test
    void testCdrPerformed_DataAccessException() {
        List<UtilsRecords.Cdr> cdrList = Collections.emptyList();
        when(properties.getMaxRetries()).thenReturn(3);
        doThrow(new DataAccessException("Database error") {}).when(bulkInserter).saveCdrBulk(cdrList);

        transactionHandler.cdrPerformed(cdrList);

        verify(bulkInserter, times(3)).saveCdrBulk(cdrList);
    }

    private UtilsRecords.Cdr createCdr() {
        return new UtilsRecords.Cdr(
                "1722968788738", "1722968788738", "1722968788735",
                "SMS", "MSG123456789", "SMPP", "NET001", "MO", "SMPP", "NET002", "MT",
                "ROUTE123", "DELIVERED", "0", "No issues", "60", "5", "7-bit",
                "2024-07-24T10:15:30", "1234567890", "1", "1", "0987654321", "1", "1",
                "REMOTE123", "LOCAL123", "SPC001", "SSN001", "GLOBAL001", "SPC002",
                "SSN002", "GLOBAL002", "IMSI001", "NNN001", "SCCP001", "MTSC001",
                "First 20 chars", "EsmClass001", "Udhi001", "Delivery001", "Ref001",
                "1", "1", "0", "Parent001"
        );
    }
}