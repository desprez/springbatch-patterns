package fr.training.springbatch.job.synchro.component;

import fr.training.springbatch.app.dto.Customer;
import fr.training.springbatch.app.dto.Transaction;
import org.springframework.batch.infrastructure.item.*;

import java.util.List;

/**
 * Read the Customer and Transaction records for a key and wrap them in a Customer object.
 */
public class MasterDetailReader implements ItemStreamReader<Customer> {

    private CustomerAccumulator masterAccumulator;
    private TransactionAccumulator detailAccumulator;

    @Override
    public Customer read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        final List<Customer> items = masterAccumulator.readNextItems();
        if (items == null || items.isEmpty()) {
            return null;
        }

        final Customer customer = items.get(0);

        final Long key = masterAccumulator.getKey(customer);
        final List<Transaction> details = detailAccumulator.readNextItems(key);

        return new Customer(customer, details);
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

    public void setMasterAccumulator(final CustomerAccumulator masterAccumulator) {
        this.masterAccumulator = masterAccumulator;
    }

    public void setDetailAccumulator(final TransactionAccumulator detailAccumulator) {
        this.detailAccumulator = detailAccumulator;
    }
}