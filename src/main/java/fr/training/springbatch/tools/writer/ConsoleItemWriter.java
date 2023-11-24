package fr.training.springbatch.tools.writer;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.lang.NonNull;

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
            System.out.println(String.format("%s%s", message, item.toString()));
        }
    }
}