package fr.training.springbatch.tools.writer;

import org.jspecify.annotations.NonNull;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;

public class ConsoleItemWriter<T> implements ItemWriter<T> {

    private String message = "";

    public ConsoleItemWriter() {

    }

    public ConsoleItemWriter(final String message) {
        this.message = message;
    }

    @Override
    public void write(final @NonNull Chunk<? extends T> items) throws Exception {
        for (final T item : items) {
            System.out.println("%s%s".formatted(message, item.toString()));
        }
    }
}