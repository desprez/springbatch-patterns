package fr.training.springbatch.job.synchro.component;

import java.util.List;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import fr.training.springbatch.tools.synchro.ItemAccumulator;

/**
 * This ItemReader reads items packets that share the same key and returns lists of items. It's use the {@link ItemAccumulator}
 *
 * @param <T>
 *            The class of the items to be processed
 * @param <K>
 *            The class of the key value of the items being processed.
 *
 * @author Desprez
 */
public class GroupReader<T, K extends Comparable<K>> implements ItemStreamReader<List<T>> {

    private ItemAccumulator<T, K> accumulator;

    @Override
    public List<T> read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        final List<T> items = accumulator.readNextItems();
        if (items == null || items.isEmpty()) {
            return null;
        }
        return items;
    }

    @Override
    public void open(final ExecutionContext executionContext) throws ItemStreamException {
        accumulator.open(executionContext);
    }

    @Override
    public void update(final ExecutionContext executionContext) throws ItemStreamException {
        accumulator.update(executionContext);
    }

    @Override
    public void close() throws ItemStreamException {
        accumulator.close();
    }

    public void setAccumulator(final ItemAccumulator<T, K> accumulator) {
        this.accumulator = accumulator;
    }

}