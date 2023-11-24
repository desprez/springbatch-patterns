package fr.training.springbatch.job.load;

import static fr.training.springbatch.tools.validator.ParameterRequirement.fileExist;
import static fr.training.springbatch.tools.validator.ParameterRequirement.required;

import java.io.File;
import java.io.IOException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.RecordFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import fr.training.springbatch.app.dto.Transaction;
import fr.training.springbatch.app.job.AbstractJobConfiguration;
import fr.training.springbatch.tools.listener.ItemCountListener;
import fr.training.springbatch.tools.listener.RejectFileSkipListener;
import fr.training.springbatch.tools.tasklet.JdbcTasklet;
import fr.training.springbatch.tools.validator.AdditiveJobParametersValidatorBuilder;
import fr.training.springbatch.tools.validator.JobParameterRequirementValidator;

/**
 * <b>Pattern #2</b> This Job load one Transaction csv file at once and insert each read line in a Transaction Table. <br>
 * <b>Nota :</b> the deleteStep is for testing purpose to remove existing Transaction records before inserting new lines.
 *
 * @author desprez
 */
@Configuration
@ConditionalOnProperty(name = "spring.batch.job.names", havingValue = SimpleLoadJobConfig.SIMPLE_LOAD_JOB)
public class SimpleLoadJobConfig extends AbstractJobConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(SimpleLoadJobConfig.class);

    protected static final String SIMPLE_LOAD_JOB = "simple-load-job";

    @Value("${application.simple-load-step.chunksize:10}")
    private int chunkSize;

    @Autowired
    private DataSource dataSource;

    @Bean
    Job simpleImportJob(final JobRepository jobRepository, final Step loadStep, final Step deleteStep) {
        return new JobBuilder(SIMPLE_LOAD_JOB, jobRepository)
                // .incrementer(new RunIdIncrementer())
                .validator(new AdditiveJobParametersValidatorBuilder()
                        .addValidator(new JobParameterRequirementValidator("input-file", required().and(fileExist())))
                        .addValidator(new JobParameterRequirementValidator("rejectfile", required()))
                        .build())
                .start(deleteStep)
                .next(loadStep)
                .listener(reportListener())
                .build();
    }

    /**
     * Delete Step for deleting all previous records.
     *
     * @return the Step
     */
    @Bean
    Step deleteStep(final JobRepository jobRepository, final PlatformTransactionManager transactionManager) {
        return new StepBuilder("delete-step", jobRepository)
                .tasklet(deletePreviousRecordTasklet(), transactionManager)
                .build();
    }

    private Tasklet deletePreviousRecordTasklet() {
        final JdbcTasklet deleteRecordTasklet = new JdbcTasklet();
        deleteRecordTasklet.setDataSource(dataSource);
        deleteRecordTasklet.setSql("DELETE FROM Transaction");
        return deleteRecordTasklet;
    }

    @Bean
    Step loadStep(final JobRepository jobRepository, final PlatformTransactionManager transactionManager, final ItemReader<Transaction> loadReader, //
            final ItemWriter<Transaction> loadWriter, final RejectFileSkipListener<Transaction, Transaction> rejectListener) {

        return new StepBuilder("simple-load-step", jobRepository)
                .<Transaction, Transaction> chunk(chunkSize, transactionManager)
                .reader(loadReader)
                .processor(loadProcessor())
                .writer(loadWriter)
                .faultTolerant()
                // .skipPolicy(new AlwaysSkipItemSkipPolicy())
                .skipLimit(100)
                .skip(RuntimeException.class)
                .listener(progressListener())
                .listener(rejectListener)
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
        return transaction -> {
            logger.debug("Processing {}", transaction);
            return transaction;
        };
    }

    @StepScope // Mandatory for using jobParameters
    @Bean
    FlatFileItemReader<Transaction> loadReader(@Value("#{jobParameters['input-file']}") final String inputFile) {

        return new FlatFileItemReaderBuilder<Transaction>()
                .name("simpleImportReader")
                .resource(new FileSystemResource(inputFile))
                .delimited()
                .delimiter(";")
                .names("customerNumber", "number", "transactionDate", "amount")
                .linesToSkip(1)
                .fieldSetMapper(new RecordFieldSetMapper<Transaction>(Transaction.class, localDateConverter()))
                .build();
    }

    @Bean
    @DependsOnDatabaseInitialization
    JdbcBatchItemWriter<Transaction> loadWriter() {

        return new JdbcBatchItemWriterBuilder<Transaction>()
                .dataSource(dataSource)
                .sql("INSERT INTO Transaction(customer_number, number, transaction_date, amount) "
                        + "VALUES (:customerNumber, :number, :transactionDate, :amount )")
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .build();
    }

    @StepScope // Mandatory for using jobParameters
    @Bean
    RejectFileSkipListener<Transaction, Transaction> rejectListener(@Value("#{jobParameters['rejectfile']}") final String rejectfile) throws IOException {
        return new RejectFileSkipListener<>(new File(rejectfile));
    }

}
