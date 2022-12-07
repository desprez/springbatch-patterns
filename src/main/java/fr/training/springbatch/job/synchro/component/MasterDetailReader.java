package fr.training.springbatch.job.synchro.component;

import java.util.List;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import fr.training.springbatch.app.dto.Customer;
import fr.training.springbatch.app.dto.Transaction;

/**
 * Read the Customer and Transaction records for a key and wrap them in a Customer object.
 */
public class MasterDetailReader implements ItemStreamReader<Customer> {

    private CustomerAccumulator masterAccumulator;
    private TransactionAccumulator detailAccumulator;

    @Override
    public Customer read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        final List<Customer> items = masterAccumulator.readNextItems();
        if (items == null || items.size() == 0) {
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