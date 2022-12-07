package fr.training.springbatch.tools.synchro;

import java.util.List;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

public class FooBarReader implements ItemStreamReader<Foo> {

    private FooAccumulator masterAccumulator;
    private BarAccumulator detailAccumulator;

    @Override
    public Foo read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        final List<Foo> items = masterAccumulator.readNextItems();
        if (items == null || items.size() == 0) {
            return null;
        }

        final Foo foo = items.get(0);

        final Integer key = masterAccumulator.getKey(foo);
        final List<Bar> details = detailAccumulator.readNextItems(key);

        return new Foo(foo, details);
    }

    @Override
    public void open(final ExecutionContext executionContext) throws ItemStreamException {
        masterAccumulator.open(executionContext);
        detailAccumulator.open(executionContext);
    }

    @Override
    public void update(final ExecutionContext executionContext) throws ItemStreamException {
        masterAccumulator.update(executionContext);
        detailAccumulator.update(executionContext);
    }

    @Override
    public void close() throws ItemStreamException {
        masterAccumulator.close();
        detailAccumulator.close();
    }

    public void setMasterAccumulator(final FooAccumulator masterAccumulator) {
        this.masterAccumulator = masterAccumulator;
    }

    public void setDetailAccumulator(final BarAccumulator detailAccumulator) {
        this.detailAccumulator = detailAccumulator;
    }

}
