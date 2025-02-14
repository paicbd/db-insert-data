package org.paic.insertdata.component;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paicbd.smsc.dto.UtilsRecords;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;

/**
 * The BulkInserter class is responsible for inserting data in bulk to the database using the provided JdbcTemplate.
 * It provides methods to save a bulk list of Records.SubmitSm and Records.DeliverSm objects asynchronously.
 */
@Slf4j
@Repository
@ConditionalOnProperty(name = "application.mode", havingValue = "database")
public class BulkInserter {
    private final JdbcTemplate jdbcTemplate;
    private final Field[] cdrCachedFiles;
    private final StringBuilder cdrSqlQuery;

    public BulkInserter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.cdrCachedFiles = getAnnotatedFields(UtilsRecords.Cdr.class);
        this.cdrSqlQuery = this.loadSqlQuery(new StringBuilder("INSERT INTO cdr ("), cdrCachedFiles);
    }

    /**
     * Saves a bulk list of Cdr records asynchronously.
     *
     * @param list the list of Cdr records to save
     */
    @Async
    @Transactional
    public void saveCdrBulk(List<UtilsRecords.Cdr> list) {
        jdbcTemplate.batchUpdate(cdrSqlQuery.toString(), list, list.size(),
                (ps, cdr) -> this.processRecordFields(ps, cdr, cdrCachedFiles)
        );
    }

    /**
     * Appends the fields annotated with @JsonProperty to the given SQL query.
     *
     * @param sql               the SQL query to be modified
     * @param objectCachedFields the fields to check for JSON property annotation
     * @return the modified SQL query with the fields appended
     */
    private StringBuilder loadSqlQuery(StringBuilder sql, Field[] objectCachedFields) {
        for (Field field : objectCachedFields) {
            JsonProperty annotation = field.getAnnotation(JsonProperty.class);
            if (annotation != null) {
                sql.append(annotation.value()).append(", ");
            }
        }
        sql.deleteCharAt(sql.length() - 2); // remove last comma
        sql.append(") VALUES ("); // add VALUES keyword
        sql.append("?, ".repeat(objectCachedFields.length)); // add ? for each field
        sql.deleteCharAt(sql.length() - 2); // remove last comma
        sql.append(")"); // add closing parenthesis

        return sql;
    }

    /**
     * Returns an array of fields in the given class that are annotated with @JsonProperty.
     *
     * @param clazz the class to retrieve the annotated fields from
     * @param <T> the type of the class
     * @return an array of fields annotated with @JsonProperty in the given class
     */
    private <T> Field[] getAnnotatedFields(Class<T> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> field.getAnnotation(JsonProperty.class) != null)
                .toArray(Field[]::new);
    }

    /**
     * Processes the record fields and sets their values into the given PreparedStatement.
     * Each field value is obtained from the given pdu object and set into the PreparedStatement using the setValueToPreparedStatement method.
     *
     * @param ps            the PreparedStatement to set the field values into
     * @param pdu           the object containing the field values
     * @param cachedFiles   the array of Field objects representing the record fields
     */
    private void processRecordFields(PreparedStatement ps, Object pdu, Field[] cachedFiles) {
        try {
            int index = 1;
            for (Field field : cachedFiles) {
                this.setValueToPreparedStatement(pdu, ps, field, index);
                index++;
            }
        } catch (Exception e) {
            log.error("Error while processing record fields", e);
        }
    }

    /**
     * Sets the value of a field to a PreparedStatement based on the provided object, field, and index.
     *
     * @param object The object from which to retrieve the field value
     * @param ps The PreparedStatement to set the field value on
     * @param field The Field object representing the field to set
     * @param index The index of the parameter in the prepared statement
     * @throws SQLException If an error occurs while setting the value on the PreparedStatement
     */
    private void setValueToPreparedStatement(Object object, PreparedStatement ps, Field field, int index) throws SQLException {
        try {
            if (!(object instanceof UtilsRecords.Cdr)) {
                throw new IllegalArgumentException("The object must be an instance of Cdr");
            }

            Method getter = object.getClass().getMethod(field.getName());
            Object value = getter.invoke(object);

            switch (field.getName()) {
                case "recordDate","submitDate","deliveryDate","dialogDuration","processingTime","remoteDialogId","localDialogId" -> setNullValueOrDefault(ps, index, value, Long.class);
                case "localSpc","localSsn","remoteSpc","remoteSsn","registeredDelivery","totalSegment","segmentSequence","retryNumber","routingId",
                     "dataCoding","addrSrcTon","addrSrcNpi","addrDstDigits","addrDstTon","addrDstNpi" -> setNullValueOrDefault(ps, index, value, Integer.class);
                default -> setNullValueOrDefault(ps, index, value, String.class);
            }
        } catch (IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
            log.error("Error setting the value to PreparedStatement", e);
        }
    }

    private void setNullValueOrDefault(PreparedStatement ps, int index, Object value, Class<?> fieldType) throws SQLException {
        if (value != null && !value.equals("")) {
            setNotNullValue(ps, index, value, fieldType);
        } else {
            setNullValue(ps, index, fieldType);
        }
    }

    private void setNotNullValue(PreparedStatement ps, int index, Object value, Class<?> fieldType) throws SQLException {
        if (fieldType == Long.class || fieldType == long.class) {
            ps.setLong(index, Long.parseLong((String) value));
        } else if (fieldType == Integer.class || fieldType == int.class) {
            ps.setInt(index, Integer.parseInt((String) value));
        } else if (fieldType == String.class) {
            ps.setString(index, (String) value);
        } else {
            ps.setObject(index, value);
        }
    }

    private void setNullValue(PreparedStatement ps, int index, Class<?> fieldType) throws SQLException {
        int sqlType;
        if (fieldType == Long.class || fieldType == long.class) {
            sqlType = Types.BIGINT;
        } else if (fieldType == Integer.class || fieldType == int.class) {
            sqlType = Types.INTEGER;
        } else if (fieldType == String.class) {
            sqlType = Types.VARCHAR;
        } else {
            sqlType = Types.OTHER;
        }
        ps.setNull(index, sqlType);
    }
}
