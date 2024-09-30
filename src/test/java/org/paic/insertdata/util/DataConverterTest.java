package org.paic.insertdata.util;

import com.paicbd.smsc.dto.UtilsRecords;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.util.AssertionErrors.assertTrue;
import static org.springframework.test.util.AssertionErrors.assertNotNull;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DataConverterTest {

    @Test
    void testPrivateConstructor() throws NoSuchMethodException {
        Constructor<DataConverter> constructor = DataConverter.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertThrows(InvocationTargetException.class, constructor::newInstance);
    }

    @Test
    void testCreateStringCdr() {
        UtilsRecords.Cdr cdr = createCdr();

        String result = DataConverter.createStringCdr(cdr, ",");

        assertNotNull(result, "The result should not be null");
        String recordDate = convertMillisecondsToDateTimeString("1722968788738");
        String submitDate = convertMillisecondsToDateTimeString("1722968788738");
        String deliveryDate = convertMillisecondsToDateTimeString("1722968788735");

        assertTrue("The result should contain the date of recordDate", result.contains(Objects.requireNonNull(recordDate)));
        assertTrue("The result should contain the date of submitDate", result.contains(Objects.requireNonNull(submitDate)));
        assertTrue("The result should contain the date of deliveryDate", result.contains(Objects.requireNonNull(deliveryDate)));
        assertTrue("The result should contain the messageType", result.contains("SMS"));
        assertTrue("The result should contain the messageId", result.contains("MSG123456789"));
        assertTrue("The result should contain the originationProtocol", result.contains("SMPP"));
        assertTrue("The result should contain the originationNetworkId", result.contains("NET001"));
        assertTrue("The result should contain the originationType", result.contains("MO"));
        assertTrue("The result should contain the destinationProtocol", result.contains("SMPP"));
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

    private static String convertMillisecondsToDateTimeString(String milliseconds) {
        try {
            long timestamp = Long.parseLong(milliseconds);
            Instant instant = Instant.ofEpochMilli(timestamp);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS").withZone(ZoneId.systemDefault());
            return formatter.format(instant);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
