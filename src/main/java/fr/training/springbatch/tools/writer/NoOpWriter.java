package fr.training.springbatch.tools.writer;

import java.util.List;

import org.springframework.batch.item.ItemWriter;

public class NoOpWriter<T> implements ItemWriter<T> {
    @Override
    public void write(final List<? extends T> items) throws Exception {
        // NO - OP
    }
}