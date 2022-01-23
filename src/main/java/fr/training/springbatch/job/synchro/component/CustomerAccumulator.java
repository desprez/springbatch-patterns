package fr.training.springbatch.job.synchro.component;

import org.springframework.batch.item.ItemReader;

import fr.training.springbatch.app.dto.Customer;
import fr.training.springbatch.tools.synchro.ItemAccumulator;

/**
 * Accumulate Customer objects.
 */
public class CustomerAccumulator extends ItemAccumulator<Customer, Long> {

	public CustomerAccumulator(final ItemReader<Customer> reader) {
		super(reader);
	}

	@Override
	public Long getKey(final Customer item) {
		return item.getNumber();
	}

}