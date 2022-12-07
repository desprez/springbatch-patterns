package fr.training.springbatch.job.multilinesrecord;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemWriter;

import fr.training.springbatch.app.dto.Customer;
import fr.training.springbatch.app.dto.Transaction;

public class MultiLineCustomerItemWriter implements ItemWriter<Customer>, ItemStream {

    private FlatFileItemWriter<String> delegate;

    @Override
    public void write(final List<? extends Customer> items) throws Exception {
        final List<String> lines = new ArrayList<>();

        for (final Customer c : items) {
            lines.add(joinString(c.getNumber().toString(), "C", c.getFirstName(), c.getLastName(), c.getAddress(), c.getCity(), c.getPostCode(), c.getState()));

            for (final Transaction t : c.getTransactions()) {
                lines.add(joinString(c.getNumber().toString(), "T", t.getNumber().toString(), t.getTransactionDate().toString(), t.getAmount().toString()));
            }
        }
        delegate.write(lines);
    }

    private String joinString(final String... tokens) {
        return Stream.of(tokens).map(n -> n.toString()).collect(Collectors.joining(","));
    }

    public void setDelegate(final FlatFileItemWriter<String> delegate) {
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