package org.paic.insertdata.component;

import com.paicbd.smsc.dto.UtilsRecords;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BulkInserterTest {
    @Mock
    private JdbcTemplate jdbcTemplate;

    private BulkInserter bulkInserter;

    @BeforeEach
    void setUp() {
        bulkInserter = new BulkInserter(jdbcTemplate);
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
}