package org.paic.insertdata.util;

import com.paicbd.smsc.dto.UtilsRecords;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import static javolution.testing.TestContext.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.AssertionErrors.fail;

@Slf4j
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BulkInserterTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private BulkInserter bulkInserter;

    private PreparedStatement preparedStatement;

    @BeforeEach
    public void setUp() {
        preparedStatement = mock(PreparedStatement.class);
    }

    @Test
    void testSaveCdrBulk() {
        UtilsRecords.Cdr cdr = createCdr();
        List<UtilsRecords.Cdr> cdrList = List.of(cdr);

        doAnswer(invocation -> {
            String sql = invocation.getArgument(0);
            BatchPreparedStatementSetter bpss = invocation.getArgument(1);

            assertTrue(sql.contains("INSERT INTO cdr"));
            assertTrue(sql.contains("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )"));

            assertEquals(cdrList.size(), bpss.getBatchSize());

            for (int i = 0; i < cdrList.size(); i++) {
                try {
                    PreparedStatement ps = mock(PreparedStatement.class);
                    bpss.setValues(ps, i);
                } catch (SQLException e) {
                    fail("SQLException occurred while verifying PreparedStatement: " + e.getMessage());
                }
            }

            return null;
        }).when(jdbcTemplate).batchUpdate(anyString(), any(BatchPreparedStatementSetter.class));

        bulkInserter.saveCdrBulk(cdrList);
    }

    @Test
    void testSetNullValueOrDefault_withNonNullValue() throws SQLException {
        bulkInserter.setNullValueOrDefault(preparedStatement, 1, "some value", String.class);

        verify(preparedStatement).setString(1, "some value");
    }

    @Test
    void testSetNullValueOrDefault_withNullValue() throws SQLException {
        bulkInserter.setNullValueOrDefault(preparedStatement, 1, "", String.class);

        verify(preparedStatement).setNull(1, Types.VARCHAR);
    }

    @Test
    void testSetNotNullValue_withLong() throws SQLException {
        bulkInserter.setNotNullValue(preparedStatement, 1, "123456789", Long.class);

        verify(preparedStatement).setLong(1, 123456789L);
    }

    @Test
    void testSetNotNullValue_withInteger() throws SQLException {
        bulkInserter.setNotNullValue(preparedStatement, 1, "123", Integer.class);

        verify(preparedStatement).setInt(1, 123);
    }

    @Test
    void testSetNotNullValue_withString() throws SQLException {
        bulkInserter.setNotNullValue(preparedStatement, 1, "test", String.class);

        verify(preparedStatement).setString(1, "test");
    }

    @Test
    void testSetNotNullValue_withOther() throws SQLException {
        Object obj = new Object();
        bulkInserter.setNotNullValue(preparedStatement, 1, obj, Object.class);

        verify(preparedStatement).setObject(1, obj);
    }

    @Test
    void testSetNullValue_withLong() throws SQLException {
        bulkInserter.setNullValue(preparedStatement, 1, Long.class);

        verify(preparedStatement).setNull(1, Types.BIGINT);
    }

    @Test
    void testSetNullValue_withInteger() throws SQLException {
        bulkInserter.setNullValue(preparedStatement, 1, Integer.class);

        verify(preparedStatement).setNull(1, Types.INTEGER);
    }

    @Test
    void testSetNullValue_withString() throws SQLException {
        bulkInserter.setNullValue(preparedStatement, 1, String.class);

        verify(preparedStatement).setNull(1, Types.VARCHAR);
    }

    @Test
    void testSetNullValue_withOther() throws SQLException {
        bulkInserter.setNullValue(preparedStatement, 1, Object.class);

        verify(preparedStatement).setNull(1, Types.OTHER);
    }

    @Test
    void testProcessRecordFields() throws Exception {
        UtilsRecords.Cdr cdr = createCdr();

        PreparedStatement ps = mock(PreparedStatement.class);

        Field[] fields = UtilsRecords.Cdr.class.getDeclaredFields();

        bulkInserter.processRecordFields(ps, cdr, fields);

        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            field.setAccessible(true);
            String fieldName = field.getName();

            switch (fieldName) {
                case "recordDate", "submitDate", "deliveryDate", "dialogDuration", "processingTime", "remoteDialogId", "localDialogId" -> {
                    long expectedValue = Long.parseLong((String) field.get(cdr));
                    verify(ps).setLong(i + 1, expectedValue);
                }
                case "localSpc", "localSsn", "remoteSpc", "remoteSsn", "registeredDelivery", "totalSegment", "segmentSequence", "retryNumber", "routingId",
                     "dataCoding", "addrSrcTon", "addrSrcNpi", "addrDstDigits", "addrDstTon", "addrDstNpi" -> {
                    int expectedValue = Integer.parseInt((String) field.get(cdr));
                    verify(ps).setInt(i + 1, expectedValue);
                }
                default -> {
                    String expectedValue = (String) field.get(cdr);
                    verify(ps).setString(i + 1, expectedValue);
                }
            }
        }
    }

    @Test
    void testSetValueToPreparedStatement() throws Exception {
        UtilsRecords.Cdr cdr = createCdr();

        Method getter = UtilsRecords.Cdr.class.getMethod("recordDate");
        Object value = getter.invoke(cdr);

        PreparedStatement ps = mock(PreparedStatement.class);

        Field field = UtilsRecords.Cdr.class.getDeclaredField("recordDate");
        field.setAccessible(true);

        bulkInserter.setValueToPreparedStatement(cdr, ps, field, 1);

        verify(ps).setLong(1, Long.parseLong((String) value));
    }

    @Test
    void testSetValueToPreparedStatement_LongField() throws Exception {
        UtilsRecords.Cdr cdr = createCdr();
        Field field = UtilsRecords.Cdr.class.getDeclaredField("recordDate");
        field.setAccessible(true);

        PreparedStatement ps = mock(PreparedStatement.class);

        bulkInserter.setValueToPreparedStatement(cdr, ps, field, 1);

        verify(ps).setLong(1, Long.parseLong((String) field.get(cdr)));
    }

    @Test
    void testSetValueToPreparedStatement_DefaultField() throws Exception {
        UtilsRecords.Cdr cdr = createCdr();
        Method getter = UtilsRecords.Cdr.class.getMethod("messageType");
        Object value = getter.invoke(cdr);

        PreparedStatement ps = mock(PreparedStatement.class);

        Field field = UtilsRecords.Cdr.class.getDeclaredField("messageType");
        field.setAccessible(true);

        bulkInserter.setValueToPreparedStatement(cdr, ps, field, 1);

        verify(ps).setString(1, (String) value);
    }

    @Test
    void testSetValueToPreparedStatement_IntegerField() throws Exception {
        UtilsRecords.Cdr cdr = createCdr();
        Field field = UtilsRecords.Cdr.class.getDeclaredField("localSpc");
        field.setAccessible(true);

        PreparedStatement ps = mock(PreparedStatement.class);

        bulkInserter.setValueToPreparedStatement(cdr, ps, field, 1);

        verify(ps).setInt(1, Integer.parseInt((String) field.get(cdr)));
    }

    private UtilsRecords.Cdr createCdr() {
        return new UtilsRecords.Cdr(
                "1722015255439",
                "1722015253291",
                "1722015255436",
                "MESSAGE",
                "1722015253291-47815692428584",
                "HTTP",
                "1",
                "SP",
                "SMPP",
                "2",
                "GW",
                "1",
                "DELIVERED",
                "SENT",
                "SENT TO GW",
                "2145",
                "2148",
                "0",
                "60",
                "50510201020",
                "1",
                "1",
                "50582368",
                "1",
                "1",
                "1",
                "1",
                "1",
                "1",
                "1",
                "1",
                "1",
                "1",
                "1",
                "1",
                "Hi PAiC",
                "0",
                "1",
                "1",
                "1",
                "1",
                "1",
                "1",
                "1",
                "1",
                "1"
        );
    }

}
