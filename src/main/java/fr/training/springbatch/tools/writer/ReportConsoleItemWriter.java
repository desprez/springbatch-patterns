package fr.training.springbatch.tools.writer;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.transform.LineAggregator;
import org.springframework.batch.item.support.AbstractItemStreamItemWriter;
import org.springframework.lang.NonNull;

/**
 * Simple {@link ItemWriter} implementation that write items to the console with header capabilities,
 */
public class ReportConsoleItemWriter<T> extends AbstractItemStreamItemWriter<T> {

    private boolean headerWritten = false;

    private String header;

    private LineAggregator<T> lineAggregator;

    @Override
    public void write(final @NonNull Chunk<? extends T> items) throws Exception {
        // need this for multi threading to stop multiple headers being written
        synchronized (this) {
            if (!headerWritten) {
                System.out.println(this.header);
                headerWritten = true;
            }
        }
        items.forEach(item -> System.out.println(this.lineAggregator.aggregate(item)));
    }

    public void setHeader(final String header) {
        this.header = header;
    }

    public void setLineAggregator(final LineAggregator<T> lineAggregator) {
        this.lineAggregator = lineAggregator;
    }
}
