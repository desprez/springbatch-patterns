package fr.training.springbatch.tools.listener;

import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.scope.context.ChunkContext;

/**
 * Log the count of items processed at a specified interval.
 */
public class ItemCountListener implements ChunkListener {

    private static final int DEFAULT_LOGGING_INTERVAL = 1000;

    private static final Logger LOGGER = LoggerFactory.getLogger(ItemCountListener.class);

    private MessageFormat fmt = new MessageFormat("{0} items processed");

    private int loggingInterval = DEFAULT_LOGGING_INTERVAL;

    @Override
    public void beforeChunk(final ChunkContext context) {
        // Nothing to do here
    }

    @Override
    public void afterChunk(final ChunkContext context) {

        final long count = context.getStepContext().getStepExecution().getReadCount();

        // If the number of records processed so far is a multiple of the logging
        // interval then output a log message.
        if (count > 0 && count % loggingInterval == 0) {
            LOGGER.info(fmt.format(new Object[] { Long.valueOf(count) }));
        }
    }

    @Override
    public void afterChunkError(final ChunkContext context) {
        // Nothing to do here
    }

    public void setItemName(final String itemName) {
        fmt = new MessageFormat("{0} " + itemName + " processed");
    }

    public void setLoggingInterval(final int loggingInterval) {
        this.loggingInterval = loggingInterval;
    }
}