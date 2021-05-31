package fr.training.springbatch.tools.writer;

import java.util.List;

import org.springframework.batch.item.ItemWriter;

public class ConsoleItemWriter<T> implements ItemWriter<T> {

	@Override
	public void write(final List<? extends T> items) throws Exception {
		for (final T item : items) {
			System.out.println(item.toString());
		}
	}
}