package org.paic.insertdata.component;

import com.paicbd.smsc.dto.UtilsRecords;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.paic.insertdata.util.AppProperties;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BulkInserterTest {
    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private AppProperties appProperties;

    private BulkInserter bulkInserter;

    @BeforeEach
    void setUp() {
        bulkInserter = new BulkInserter(jdbcTemplate, appProperties);
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("Update batch when config is correctly then do it")
    void updateBatchWhenConfigIsCorrectlyThenDoIt() throws SQLException {
        List<UtilsRecords.Cdr> cdrList = Arrays.asList(ObjectsCreator.getDefaultCdr(), ObjectsCreator.getDefaultCdr());
        ArgumentCaptor<String> sqlQueryCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<List<UtilsRecords.Cdr>> listCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Integer> batchSizeCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<ParameterizedPreparedStatementSetter<UtilsRecords.Cdr>> setterCaptor =
                ArgumentCaptor.forClass(ParameterizedPreparedStatementSetter.class);

        bulkInserter.saveCdrBulk(cdrList);
        verify(jdbcTemplate)
                .batchUpdate(sqlQueryCaptor.capture(), listCaptor.capture(), batchSizeCaptor.capture(), setterCaptor.capture());

        var sqlQuery = sqlQueryCaptor.getValue();
        var list = listCaptor.getValue();
        var batchSize = batchSizeCaptor.getValue();
        var setter = setterCaptor.getValue();

        assertNotNull(sqlQuery);
        assertNotNull(list);
        assertNotNull(batchSize);
        assertNotNull(setter);

        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
        setter.setValues(mockPreparedStatement, cdrList.getFirst());

        assertTrue(sqlQuery.contains("INSERT INTO cdr ("));
        assertEquals(2, batchSize);
        assertEquals(2, list.size());

        System.out.println("sqlQuery = " + sqlQuery);
        System.out.println("list = " + list);
        System.out.println("batchSize = " + batchSize);
        System.out.println("setter = " + setter);
    }

    @Test
    @DisplayName("Should handle exception inside processRecordFields when object is not a Cdr")
    void processRecordFieldsShouldCatchExceptionForInvalidObject() throws Exception {
        Field[] fields = UtilsRecords.Cdr.class.getDeclaredFields();
        Object invalidObject = new Object();
        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);

        Method method = BulkInserter.class.getDeclaredMethod(
                "processRecordFields",
                PreparedStatement.class,
                Object.class,
                Field[].class
        );
        method.setAccessible(true);

        method.invoke(bulkInserter, mockPreparedStatement, invalidObject, fields);
        assertNotNull(method);
    }

    @Test
    @DisplayName("setNotNullValue should use default case for unsupported type")
    void setNotNullValueShouldUseDefaultCaseForUnsupportedType() throws Exception {
        class CustomType {
            @Override
            public String toString() {
                return "custom";
            }
        }

        PreparedStatement ps = mock(PreparedStatement.class);
        Object value = new CustomType();

        Method method = BulkInserter.class.getDeclaredMethod(
                "setNotNullValue", PreparedStatement.class, int.class, Object.class, Class.class
        );
        method.setAccessible(true);

        method.invoke(bulkInserter, ps, 1, value, CustomType.class);
        verify(ps).setObject(1, value);
    }

    @Test
    @DisplayName("processMessage should use full message when messageLength is -1")
    void processMessageShouldUseFullMessageWhenMessageLengthIsMinusOne() throws Exception {
        when(appProperties.getCdrMessageLength()).thenReturn(-1);

        Method method = BulkInserter.class.getDeclaredMethod(
                "processMessage", PreparedStatement.class, int.class, Object.class
        );
        method.setAccessible(true);

        PreparedStatement ps = mock(PreparedStatement.class);
        String originalMessage = "Hello, this is a long test message.";

        method.invoke(bulkInserter, ps, 1, originalMessage);
        verify(ps).setString(1, originalMessage);
    }

    @Test
    @DisplayName("processMessage should truncate message when messageLength is 20")
    void processMessageShouldTruncateMessageWhenMessageLengthIs20() throws Exception {
        when(appProperties.getCdrMessageLength()).thenReturn(20);

        Method method = BulkInserter.class.getDeclaredMethod(
                "processMessage", PreparedStatement.class, int.class, Object.class
        );
        method.setAccessible(true);

        PreparedStatement ps = mock(PreparedStatement.class);
        String originalMessage = "Hello, this is a long test message.";

        method.invoke(bulkInserter, ps, 1, originalMessage);
        verify(ps).setString(1, "Hello, this is a lon");
    }

    @Test
    @DisplayName("processMessage should remove message when messageLength is 0")
    void processMessageShouldRemoveMessageWhenMessageLengthIsZero() throws Exception {
        when(appProperties.getCdrMessageLength()).thenReturn(0);

        Method method = BulkInserter.class.getDeclaredMethod(
                "processMessage", PreparedStatement.class, int.class, Object.class
        );
        method.setAccessible(true);

        PreparedStatement ps = mock(PreparedStatement.class);
        String originalMessage = "Hello, this is a long test message.";

        method.invoke(bulkInserter, ps, 1, originalMessage);
        verify(ps).setString(1, "");
    }

    @Test
    @DisplayName("setNullValue should use VARCHAR and OTHER based on type")
    void setNullValueShouldHandleVarcharAndOtherTypes() throws Exception {
        PreparedStatement ps = mock(PreparedStatement.class);

        Method method = BulkInserter.class.getDeclaredMethod(
                "setNullValue", PreparedStatement.class, int.class, Class.class
        );
        method.setAccessible(true);

        method.invoke(bulkInserter, ps, 1, String.class);
        verify(ps).setNull(1, java.sql.Types.VARCHAR);

        method.invoke(bulkInserter, ps, 2, Double.class);
        verify(ps).setNull(2, java.sql.Types.OTHER);
    }
}