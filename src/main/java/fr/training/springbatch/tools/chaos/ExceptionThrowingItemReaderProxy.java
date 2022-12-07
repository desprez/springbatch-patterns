package fr.training.springbatch.tools.chaos;

import org.springframework.batch.core.UnexpectedJobExecutionException;
import org.springframework.batch.item.ItemReader;
import org.springframework.lang.Nullable;

/**
 * Hacked {@link ItemReader} that throws exception on a given record number (useful for testing restart).
 *
 * @author Robert Kasanicky
 * @author Lucas Ward
 *
 */
public class ExceptionThrowingItemReaderProxy<T> implements ItemReader<T> {

    private int counter = 0;

    private int throwExceptionOnRecordNumber = 4;

    private ItemReader<T> delegate;

    /**
     * @param throwExceptionOnRecordNumber
     *            The number of record on which exception should be thrown
     */
    public void setThrowExceptionOnRecordNumber(final int throwExceptionOnRecordNumber) {
        this.throwExceptionOnRecordNumber = throwExceptionOnRecordNumber;
    }

    @Nullable
    @Override
    public T read() throws Exception {

        counter++;
        if (counter == throwExceptionOnRecordNumber) {
            throw new UnexpectedJobExecutionException("Planned failure on count=" + counter);
        }

        return delegate.read();
    }

    public void setDelegate(final ItemReader<T> delegate) {
        this.delegate = delegate;
    }

}