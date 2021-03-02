package fr.training.springbatch.job.synchrojob.component;

import org.springframework.batch.item.ItemReader;

import fr.training.springbatch.app.dto.Transaction;
import fr.training.springbatch.tools.synchro.ItemAccumulator;

public class TransactionAccumulator extends ItemAccumulator<Transaction, String> {

	public TransactionAccumulator(final ItemReader<Transaction> reader) {
		super(reader);
	}

	@Override
	public String getKey(final Transaction item) {
		return item.getCustomerNumber();
	}

}
