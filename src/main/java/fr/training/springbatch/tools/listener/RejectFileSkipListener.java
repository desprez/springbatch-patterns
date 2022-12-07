package fr.training.springbatch.tools.listener;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.item.file.FlatFileParseException;

/**
 * A {@link SkipListener} that save skipped reader lines in rejection file. <br>
 * Note : this class must be used as a bean to gracefully close FileWriter after dispose.
 *
 * @param <T>
 *            the target failed item for processor
 * @param <S>
 *            the source failed item for writer
 */
public class RejectFileSkipListener<T, S> implements SkipListener<T, S>, Closeable {

    private static final Logger logger = LoggerFactory.getLogger(RejectFileSkipListener.class);

    private final FileWriter fileWriter;

    public RejectFileSkipListener(final File file) throws IOException {
        fileWriter = new FileWriter(file);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.batch.core.SkipListener#onSkipInRead(java.lang.Throwable)
     */
    @Override
    public void onSkipInRead(final Throwable throwable) {
        if (throwable instanceof FlatFileParseException) {
            final FlatFileParseException ffpe = (FlatFileParseException) throwable;
            try {
                logger.info("{}: {} -- {}", ffpe.getLineNumber(), ffpe.getInput(), ffpe.getMessage());
                fileWriter.write(ffpe.getInput());
                fileWriter.write("\n");
            } catch (final IOException e) {
                logger.error("Unable to write skipped line to error file");
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.batch.core.SkipListener#onSkipInWrite(java.lang.Object, java.lang.Throwable)
     */
    @Override
    public void onSkipInWrite(final S item, final Throwable t) {
        logger.error("Item {} was skipped due to: {} ", item, t.getMessage());
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.batch.core.SkipListener#onSkipInProcess(java.lang.Object, java.lang.Throwable)
     */
    @Override
    public void onSkipInProcess(final T item, final Throwable t) {
        logger.error("Item {} was skipped due to: {} ", item, t.getMessage());
    }

    /*
     * (non-Javadoc)
     * @see java.io.Closeable#close()
     */
    @Override
    public void close() throws IOException {
        logger.info("Closing writer");
        fileWriter.close();
    }
}