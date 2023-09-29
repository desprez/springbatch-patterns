package fr.training.springbatch.job.synchro;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import fr.training.springbatch.app.dto.Customer;
import fr.training.springbatch.app.dto.Transaction;
import fr.training.springbatch.job.synchro.component.MasterDetailReader;
import fr.training.springbatch.tools.synchro.CompositeAggregateReader;
import fr.training.springbatch.tools.synchro.ItemAccumulator;

/**
 * Using {@link ItemAccumulator} & {@link MasterDetailReader} to "synchronize" 2 flat files that share the same "customer number" key.
 * <ul>
 * <li>one master file : customer csv file</li>
 * <li>one detail file : transaction csv file</li>
 * </ul>
 *
 * Datas from the detail file (transaction) are stored in the item (customer) returned by the "Master" reader.
 *
 * @author Desprez
 */
@Configuration
@ConditionalOnProperty(name = "spring.batch.job.names", havingValue = File2FileSynchroJobConfig.FILE2FILE_SYNCHRO_JOB)
public class File2FileSynchroJobConfig extends AbstractSynchroJob {

    private static final Logger logger = LoggerFactory.getLogger(File2FileSynchroJobConfig.class);

    protected static final String FILE2FILE_SYNCHRO_JOB = "file2filesynchro-job";

    @Value("${application.file2filesynchro-step.chunksize:10}")
    private int chunkSize;

    /**
     * @param file2FileSynchroStep
     *            the injected Step bean
     * @return the job bean
     */
    @Bean
    Job file2FileSynchroJob(final Step file2FileSynchroStep, final JobRepository jobRepository) {
        return new JobBuilder(FILE2FILE_SYNCHRO_JOB, jobRepository) //
                .incrementer(new RunIdIncrementer()) // job can be launched as many times as desired
                .validator(new DefaultJobParametersValidator(new String[] { "customer-file", "transaction-file", "output-file" }, new String[] {})) //
                .start(file2FileSynchroStep) //
                .listener(reportListener()) //
                .build();
    }

    /**
     * @param masterDetailReader
     *            the injected {@link MasterDetailReader}
     * @param customerWriter
     *            the injected Customer ItemWriter
     * @return a Step Bean
     */
    @Bean
    Step file2FileSynchroStep(final JobRepository jobRepository, final PlatformTransactionManager transactionManager,
            final CompositeAggregateReader<Customer, Transaction, Long> masterDetailReader, final ItemWriter<Customer> customerWriter) {

        return new StepBuilder("file2filesynchro-step", jobRepository) //
                .<Customer, Customer> chunk(chunkSize, transactionManager) //
                .reader(masterDetailReader) //
                .processor(processor()) //
                .writer(customerWriter) //
                .listener(reportListener()) //
                .build();
    }

    /**
     * @param customerFile
     *            the injected customer file job parameter
     * @return a {@link FlatFileItemReader} bean
     */
    @StepScope // Mandatory for using jobParameters
    @Bean
    FlatFileItemReader<Customer> customerReader(@Value("#{jobParameters['customer-file']}") final String customerFile) {

        return new FlatFileItemReaderBuilder<Customer>() //
                .name("customerReader") //
                .resource(new FileSystemResource(customerFile)) //
                .delimited() //
                .delimiter(";") //
                .names("number", "firstName", "lastName", "address", "city", "state", "postCode") //
                .linesToSkip(1) //
                .targetType(Customer.class) //
                .build();
    }

    @StepScope // Mandatory for using jobParameters
    @Bean
    FlatFileItemReader<Transaction> transactionReader(@Value("#{jobParameters['transaction-file']}") final String transactionFile /* injected by Spring */) {

        return new FlatFileItemReaderBuilder<Transaction>() //
                .name("transactionReader") //
                .resource(new FileSystemResource(transactionFile)) //
                .delimited() //
                .delimiter(";") //
                .names("customerNumber", "number", "transactionDate", "amount") //
                .linesToSkip(1) //
                .fieldSetMapper(new BeanWrapperFieldSetMapper<Transaction>() {
                    {
                        setTargetType(Transaction.class);
                        setConversionService(localDateConverter());
                    }
                }).build();
    }

    /**
     * Processor that sum customer's transactions to compute his balance.
     *
     * @return the processor
     */
    private ItemProcessor<Customer, Customer> processor() {
        return customer -> {
            final double sum = customer.getTransactions().stream().mapToDouble(Transaction::getAmount).sum();
            customer.setBalance(new BigDecimal(sum).setScale(2, RoundingMode.HALF_UP).doubleValue());
            logger.debug(customer.toString());
            return customer;
        };
    }

    /**
     * @param outputFile
     *            the injected output file job parameter
     * @return a {@link FlatFileItemWriter} bean
     */
    @StepScope // Mandatory for using jobParameters
    @Bean
    FlatFileItemWriter<Customer> customerWriter(@Value("#{jobParameters['output-file']}") final String outputFile) {

        return new FlatFileItemWriterBuilder<Customer>().name("customerWriter").resource(new FileSystemResource(outputFile)) //
                .delimited() //
                .delimiter(";") //
                .names("number", "firstName", "lastName", "address", "city", "state", "postCode", "balance") //
                .build();

    }

}
