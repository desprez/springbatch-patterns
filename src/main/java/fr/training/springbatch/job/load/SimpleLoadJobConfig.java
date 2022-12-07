package fr.training.springbatch.job.load;

import java.io.File;
import java.io.IOException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;

import fr.training.springbatch.app.dto.Transaction;
import fr.training.springbatch.app.job.AbstractJobConfiguration;
import fr.training.springbatch.tools.listener.ItemCountListener;
import fr.training.springbatch.tools.listener.RejectFileSkipListener;
import fr.training.springbatch.tools.tasklet.JdbcTasklet;

/**
 * This Job load one Transaction csv file at once and insert each read line in a Transaction Table.
 *
 * Nota : the deleteStep is for testing purpose to remove existing Transaction records before inserting new lines.
 *
 * @author desprez
 */
public class SimpleLoadJobConfig extends AbstractJobConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(SimpleLoadJobConfig.class);

    @Value("${application.simple-load-step.chunksize:10}")
    private int chunkSize;

    @Autowired
    private DataSource dataSource;

    @Bean
    public Job simpleImportJob(final Step loadStep) {
        return jobBuilderFactory.get("simple-load-job") //
                // .incrementer(new RunIdIncrementer()) //
                .validator(new DefaultJobParametersValidator(new String[] { "input-file", "rejectfile" }, new String[] {})) //
                .start(deleteStep()) //
                .next(loadStep) //
                .listener(reportListener()) //
                .build();
    }

    /**
     * Delete Step for deleting all previous records.
     *
     * @return the Step
     */
    @Bean
    public Step deleteStep() {
        return stepBuilderFactory.get("delete-step") //
                .tasklet(deletePreviousRecordTasklet()) //
                .build();
    }

    private Tasklet deletePreviousRecordTasklet() {
        final JdbcTasklet deleteRecordTasklet = new JdbcTasklet();
        deleteRecordTasklet.setDataSource(dataSource);
        deleteRecordTasklet.setSql("DELETE FROM Transaction");
        return deleteRecordTasklet;
    }

    @Bean
    public Step loadStep(final ItemReader<Transaction> loadReader, //
            final ItemWriter<Transaction> loadWriter, final RejectFileSkipListener<Transaction, Transaction> rejectListener) {

        return stepBuilderFactory.get("simple-load-step") //
                .<Transaction, Transaction> chunk(chunkSize) //
                .reader(loadReader) //
                .processor(loadProcessor()) //
                .writer(loadWriter) //
                .faultTolerant() //
                // .skipPolicy(new AlwaysSkipItemSkipPolicy())
                .skipLimit(100) //
                .skip(RuntimeException.class) //
                .listener(progressListener()) //
                .listener(rejectListener) //
                .build();
    }

    /**
     * Used for logging step progression
     */
    @Override
    @Bean
    public ItemCountListener progressListener() {
        final ItemCountListener listener = new ItemCountListener();
        listener.setItemName("Transaction(s)");
        listener.setLoggingInterval(50); // Log process item count every 50
        return listener;
    }

    /**
     * Fake processor that only logs
     *
     * @return an item processor
     */
    private ItemProcessor<Transaction, Transaction> loadProcessor() {
        return new ItemProcessor<Transaction, Transaction>() {

            @Override
            public Transaction process(final Transaction transaction) throws Exception {
                logger.debug("Processing {}", transaction);
                return transaction;
            }
        };
    }

    @StepScope // Mandatory for using jobParameters
    @Bean
    public FlatFileItemReader<Transaction> loadReader(@Value("#{jobParameters['input-file']}") final String inputFile) {

        return new FlatFileItemReaderBuilder<Transaction>() //
                .name("simpleImportReader") //
                .resource(new FileSystemResource(inputFile)) //
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

    @Bean
    public JdbcBatchItemWriter<Transaction> loadWriter() {

        return new JdbcBatchItemWriterBuilder<Transaction>() //
                .dataSource(dataSource)
                .sql("INSERT INTO Transaction(customer_number, number, transaction_date, amount) "
                        + "VALUES (:customerNumber, :number, :transactionDate, :amount )")
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<Transaction>()) //
                .build();
    }

    @StepScope // Mandatory for using jobParameters
    @Bean
    public RejectFileSkipListener<Transaction, Transaction> rejectListener(@Value("#{jobParameters['rejectfile']}") final String rejectfile)
            throws IOException {

        return new RejectFileSkipListener<Transaction, Transaction>(new File(rejectfile));
    }

}
