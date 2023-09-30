package fr.training.springbatch.tools.writer;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

public class NoOpWriter<T> implements ItemWriter<T> {

    @Override
    public void write(final Chunk<? extends T> items) throws Exception {
        // NO - OP
    }
}