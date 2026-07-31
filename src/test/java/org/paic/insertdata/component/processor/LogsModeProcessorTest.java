package org.paic.insertdata.component.processor;

import com.paicbd.smsc.dto.UtilsRecords;
import com.paicbd.smsc.utils.Converter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.paic.insertdata.component.LogsComponent;
import org.paic.insertdata.component.ObjectsCreator;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

class LogsModeProcessorTest {

    @Test
    @DisplayName("Should send CDR batch to LogsComponent when processing raw JSON")
    void shouldSendCdrBatchToLogsComponent() {
        UtilsRecords.Cdr cdr = ObjectsCreator.getDefaultCdr();
        String json = Converter.valueAsString(cdr);
        List<String> rawList = List.of(json);

        LogsComponent mockLogsComponent = mock(LogsComponent.class);
        LogsModeProcessor processor = new LogsModeProcessor(mockLogsComponent);

        AtomicReference<Flux<List<UtilsRecords.Cdr>>> logsFluxRef = new AtomicReference<>();
        doAnswer(inv -> {
            Flux<List<UtilsRecords.Cdr>> flux = inv.getArgument(0);
            logsFluxRef.set(flux);
            return null;
        }).when(mockLogsComponent).storeInLogFile(any());

        processor.processCdr(rawList);

        verify(mockLogsComponent, timeout(2000)).storeInLogFile(any());

        StepVerifier.create(logsFluxRef.get().next())
                .assertNext(list -> {
                    assert list != null;
                    assert list.size() == 1;
                    String firstId = list.stream()
                            .findFirst()
                            .map(UtilsRecords.Cdr::messageId)
                            .orElse(null);
                    org.junit.jupiter.api.Assertions.assertEquals(cdr.messageId(), firstId);
                })
                .verifyComplete();
    }
}