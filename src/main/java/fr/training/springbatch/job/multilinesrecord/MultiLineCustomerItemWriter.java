package fr.training.springbatch.job.multilinesrecord;

import fr.training.springbatch.app.dto.Customer;
import fr.training.springbatch.app.dto.Transaction;
import org.jspecify.annotations.NonNull;
import org.springframework.batch.infrastructure.item.*;
import org.springframework.batch.infrastructure.item.file.FlatFileItemWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MultiLineCustomerItemWriter implements ItemWriter<Customer>, ItemStream {

    private FlatFileItemWriter<String> delegate;

    @Override
    public void write(@NonNull final Chunk<? extends Customer> items) throws Exception {
        final List<String> lines = new ArrayList<>();

        for (final Customer c : items) {
            lines.add(joinString(c.getNumber().toString(), "C", c.getFirstName(), c.getLastName(), c.getAddress(), c.getCity(), c.getPostCode(), c.getState()));

            for (final Transaction t : c.getTransactions()) {
                lines.add(joinString(c.getNumber().toString(), "T", t.number(), t.transactionDate().toString(), t.amount().toString()));
            }
        }
        delegate.write(new Chunk<>(lines));
    }

    private String joinString(final String... tokens) {
        return Stream.of(tokens).map(n -> n).collect(Collectors.joining(","));
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