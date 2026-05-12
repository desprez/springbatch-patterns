package fr.training.springbatch.tools.chaos;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.job.UnexpectedJobExecutionException;
import org.springframework.batch.infrastructure.item.ItemProcessor;

public class ExceptionThrowingProcessor<T> implements ItemProcessor<T, T> {

    private static final Logger log = LoggerFactory.getLogger(ExceptionThrowingProcessor.class);

    private int counter;

    private int throwExceptionOnNumber = Integer.MAX_VALUE;

    @Override
    public T process(final @NonNull T item) throws Exception {
        counter++;
        if (counter == throwExceptionOnNumber) {
            throw new UnexpectedJobExecutionException("Planned failure on count=" + counter);
        }
        log.debug("Process count {}", counter);
        return item;
    }

    public void setThrowExceptionOnNumber(final int throwExceptionOnNumber) {
        this.throwExceptionOnNumber = throwExceptionOnNumber;
    }

}