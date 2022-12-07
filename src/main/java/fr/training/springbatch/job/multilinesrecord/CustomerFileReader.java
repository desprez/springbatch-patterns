package fr.training.springbatch.job.multilinesrecord;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import fr.training.springbatch.app.dto.Customer;
import fr.training.springbatch.app.dto.Transaction;

public class CustomerFileReader implements ItemStreamReader<Customer> {

    private Object curItem = null;

    private ItemStreamReader<Object> delegate;

    @Override
    public Customer read() throws Exception {
        if (curItem == null) {
            curItem = delegate.read();
        }

        final Customer customer = (Customer) curItem;
        curItem = null;

        if (customer != null) {

            while (peek() instanceof Transaction) {
                customer.addTransaction((Transaction) curItem);
                curItem = null;
            }
        }
        return customer;
    }

    public Object peek() throws Exception, UnexpectedInputException, ParseException {
        if (curItem == null) {
            curItem = delegate.read();
        }
        return curItem;
    }

    public void setDelegate(final ItemStreamReader<Object> delegate) {
        this.delegate = delegate;
    }

    @Override
    public void close() throws ItemStreamException {
        delegate.close();
    }

    @Override
    public void open(final ExecutionContext executionContext) throws ItemStreamException {
        delegate.open(executionContext);
    }

    @Override
    public void update(final ExecutionContext executionContext) throws ItemStreamException {
        delegate.update(executionContext);
    }

}