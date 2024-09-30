package org.paic.insertdata.util;

import com.paicbd.smsc.dto.UtilsRecords;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
public class DataConverter {

    private DataConverter() {
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
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS").withZone(ZoneId.systemDefault());
            return formatter.format(instant);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
