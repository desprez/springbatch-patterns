package fr.training.springbatch.tools.writer;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.lang.NonNull;

public class NoOpWriter<T> implements ItemWriter<T> {

    @Override
    public void write(final @NonNull Chunk<? extends T> items) throws Exception {
        // NO - OP
    }
}