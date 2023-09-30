package fr.training.springbatch.tools.synchro;

import org.springframework.batch.item.ItemReader;

public class FooAccumulator extends ItemAccumulator<Foo, Integer> {

    public FooAccumulator(final ItemReader<Foo> reader) {
        super(reader);
    }

    @Override
    public Integer getKey(final Foo item) {
        return item.getId();
    }
}
