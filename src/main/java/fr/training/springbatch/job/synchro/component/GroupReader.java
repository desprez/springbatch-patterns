package fr.training.springbatch.job.synchro.component;

import fr.training.springbatch.tools.synchro.ItemAccumulator;
import org.jspecify.annotations.NonNull;
import org.springframework.batch.infrastructure.item.*;

import java.util.List;

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
    public List<T> read() throws Exception {
        final List<T> items = accumulator.readNextItems();
        if (items == null || items.isEmpty()) {
            return null;
        }
        return items;
    }

    @Override
    public void open(@NonNull ExecutionContext executionContext) throws ItemStreamException {
        accumulator.open(executionContext);
    }

    @Override
    public void update(@NonNull ExecutionContext executionContext) throws ItemStreamException {
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