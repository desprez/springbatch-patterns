package fr.training.springbatch.tools.writer;

import org.jspecify.annotations.NonNull;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;

public class NoOpWriter<T> implements ItemWriter<T> {

    @Override
    public void write(final @NonNull Chunk<? extends T> items) throws Exception {
        // NO - OP
    }
}