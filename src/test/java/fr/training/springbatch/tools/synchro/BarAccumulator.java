package fr.training.springbatch.tools.synchro;

import org.springframework.batch.item.ItemReader;

public class BarAccumulator extends ItemAccumulator<Bar, Integer> {

    public BarAccumulator(final ItemReader<Bar> reader) {
        super(reader);
    }

    @Override
    public Integer getKey(final Bar item) {
        return item.getFooId();
    }
}