package fr.training.springbatch.job.synchro;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.AbstractItemStreamItemReader;
import org.springframework.context.annotation.Bean;

import fr.training.springbatch.app.dto.Customer;
import fr.training.springbatch.app.dto.Transaction;
import fr.training.springbatch.app.job.AbstractJobConfiguration;
import fr.training.springbatch.job.synchro.component.MasterDetailReader;
import fr.training.springbatch.tools.synchro.CompositeAggregateReader;

public abstract class AbstractSynchroJob extends AbstractJobConfiguration {

    public AbstractSynchroJob() {
    }

    /**
     * Delegate pattern reader
     *
     * @param customerReader
     *            the injected Customer {@link ItemReader} bean
     * @param transactionReader
     *            the injected Transaction {@link ItemReader} bean
     * @return a {@link MasterDetailReader} bean
     */
    @Bean(destroyMethod = "")
    CompositeAggregateReader<Customer, Transaction, Long> masterDetailReader(final AbstractItemStreamItemReader<Customer> customerReader,
            final AbstractItemStreamItemReader<Transaction> transactionReader) {

        final CompositeAggregateReader<Customer, Transaction, Long> masterDetailReader = new CompositeAggregateReader<>();
        masterDetailReader.setMasterItemReader(customerReader);
        masterDetailReader.setMasterKeyExtractor(Customer::getNumber);
        masterDetailReader.setSlaveItemReader(transactionReader);
        masterDetailReader.setSlaveKeyExtractor(Transaction::customerNumber);
        masterDetailReader.setMasterAggregator(Customer::addTransaction);

        return masterDetailReader;
    }
}