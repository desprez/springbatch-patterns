package fr.training.springbatch.job.synchro.component;

import fr.training.springbatch.app.dto.Transaction;
import fr.training.springbatch.tools.synchro.ItemAccumulator;
import org.springframework.batch.infrastructure.item.ItemReader;

public class TransactionAccumulator extends ItemAccumulator<Transaction, Long> {

    public TransactionAccumulator(final ItemReader<Transaction> reader) {
        super(reader);
    }

    @Override
    public Long getKey(final Transaction item) {
        return item.customerNumber();
    }

}
