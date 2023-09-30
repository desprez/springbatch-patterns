package fr.training.springbatch.tools.chaos;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.UnexpectedJobExecutionException;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.context.RepeatContextSupport;
import org.springframework.batch.repeat.support.RepeatSynchronizationManager;

class ExceptionThrowingItemReaderProxyTest {

    // expected call count before exception is thrown (exception should be thrown in
    // next iteration)
    private static final int ITER_COUNT = 5;

    @AfterEach
    void tearDown() throws Exception {
        RepeatSynchronizationManager.clear();
    }

    @SuppressWarnings("serial")
    @Test
    void testProcess() throws Exception {

        // create module and set item processor and iteration count
        final ExceptionThrowingItemReaderProxy<String> itemReader = new ExceptionThrowingItemReaderProxy<>();
        itemReader.setDelegate(new ListItemReader<>(new ArrayList<String>() {
            {
                add("a");
                add("b");
                add("c");
                add("d");
                add("e");
                add("f");
            }
        }));

        itemReader.setThrowExceptionOnRecordNumber(ITER_COUNT + 1);

        RepeatSynchronizationManager.register(new RepeatContextSupport(null));

        // call process method multiple times and verify whether exception is thrown
        // when expected
        for (int i = 0; i <= ITER_COUNT; i++) {
            try {
                itemReader.read();
                assertThat(i).isLessThan(ITER_COUNT);
            } catch (final UnexpectedJobExecutionException bce) {
                assertThat(i).isEqualTo(ITER_COUNT);
            }
        }

    }
}