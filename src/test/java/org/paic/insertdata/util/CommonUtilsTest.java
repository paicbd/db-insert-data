package org.paic.insertdata.util;

import com.paicbd.smsc.dto.UtilsRecords;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.paic.insertdata.component.ObjectsCreator;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class CommonUtilsTest {

    @Test
    @DisplayName("Should create string cdr with full message when messageLength is -1")
    void createStringCdrWhenMessageLengthIsMinusOneThenUseFullMessage() {
        UtilsRecords.Cdr cdr = ObjectsCreator.getDefaultCdr();
        String result = CommonUtils.createStringCdr(cdr, "|", -1);
        assertTrue(result.contains("MESSAGE|1734454582187-9026385306105|mno-message-id|HTTP|3|Origination Network Name|SP|SS7|2|Destination Network Name|GW|1||SENT||1773|1775|0|60|1322888089|1|1|0987654321|1|1||73|0|8|22220|200|8|55566768|550e8400e29b41d4a716446655440000|730169999999212|55566768||22220|This is a long message, with this we can test the reduce process.|0||0|||||1734454582187-9026385306105"));
    }

    @Test
    @DisplayName("Should create string cdr with truncated message when messageLength is 20")
    void createStringCdrWhenMessageLengthIs20ThenTruncateMessage() {
        UtilsRecords.Cdr cdr = ObjectsCreator.getDefaultCdr();
        String result = CommonUtils.createStringCdr(cdr, "|", 20);
        assertTrue(result.contains("MESSAGE|1734454582187-9026385306105|mno-message-id|HTTP|3|Origination Network Name|SP|SS7|2|Destination Network Name|GW|1||SENT||1773|1775|0|60|1322888089|1|1|0987654321|1|1||73|0|8|22220|200|8|55566768|550e8400e29b41d4a716446655440000|730169999999212|55566768||22220|This is a long messa|0||0|||||1734454582187-9026385306105"));
    }

    @Test
    @DisplayName("Should create string cdr with no message when messageLength is 0")
    void createStringCdrWhenMessageLengthIsZeroThenRemoveMessage() {
        UtilsRecords.Cdr cdr = ObjectsCreator.getDefaultCdr();
        String result = CommonUtils.createStringCdr(cdr, "|", 0);
        assertTrue(result.contains("MESSAGE|1734454582187-9026385306105|mno-message-id|HTTP|3|Origination Network Name|SP|SS7|2|Destination Network Name|GW|1||SENT||1773|1775|0|60|1322888089|1|1|0987654321|1|1||73|0|8|22220|200|8|55566768|550e8400e29b41d4a716446655440000|730169999999212|55566768||22220||0||0|||||1734454582187-9026385306105"));
        String[] fields = result.split("\\|");
        String messageField = fields[39];
        assertEquals("", messageField);
    }

    @Test
    @DisplayName("Should return null for invalid milliseconds string using reflection")
    void convertMillisecondsToDateTimeStringInvalid() throws Exception {
        Method method = CommonUtils.class.getDeclaredMethod("convertMillisecondsToDateTimeString", String.class);
        method.setAccessible(true);

        String input = "invalid";
        String result = (String) method.invoke(null, input);

        assertNull(result);
    }

    @ParameterizedTest
    @DisplayName("Should truncate message based on specified length")
    @CsvSource({
            "'Short message',-1,Short message",
            "'Short message',0,''",
            "'Short message',5,Short",
            "'Exactly 20 chars!!',20,Exactly 20 chars!!",
            "'This is a very long message that should be truncated',10,'This is a '",
            "'This is a very long message that should be truncated',100,'This is a very long message that should be truncated'"
    })
    void shouldTruncateMessageBasedOnLength(String input, int length, String expected) {
        String result = CommonUtils.truncateMessage(input, length);
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("Should handle null and empty messages in truncateMessage")
    void truncateMessageShouldHandleNullAndEmpty() {
        assertNull(CommonUtils.truncateMessage(null, -1));
        assertEquals("", CommonUtils.truncateMessage("", 10));
        assertEquals("", CommonUtils.truncateMessage("", 0));
    }

    @ParameterizedTest
    @DisplayName("Should truncate message to 20 characters when length is 20")
    @CsvSource({
            "'Short message',Short message",
            "'Exactly 20 chars!!',Exactly 20 chars!!",
            "'This is a very long message that should be reduced','This is a very long '"
    })
    void shouldTruncateMessageTo20CharactersWhenLengthIs20(String input, String expected) {
        String result = CommonUtils.truncateMessage(input, 20);
        assertEquals(expected, result);
    }
}
