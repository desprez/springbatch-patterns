package fr.training.springbatch.job.staging;

import static fr.training.springbatch.tools.validator.ParameterRequirement.fileExist;
import static fr.training.springbatch.tools.validator.ParameterRequirement.required;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.support.DataFieldMaxValueIncrementerFactory;
import org.springframework.batch.item.database.support.DefaultDataFieldMaxValueIncrementerFactory;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.validator.SpringValidator;
import org.springframework.batch.item.validator.ValidatingItemProcessor;
import org.springframework.batch.support.DatabaseType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.support.MetaDataAccessException;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;
import org.springframework.transaction.PlatformTransactionManager;

import fr.training.springbatch.app.dto.Transaction;
import fr.training.springbatch.app.job.AbstractJobConfiguration;
import fr.training.springbatch.tools.listener.ItemCountListener;
import fr.training.springbatch.tools.staging.ProcessIndicatorItemWrapper;
import fr.training.springbatch.tools.staging.StagingItemProcessor;
import fr.training.springbatch.tools.staging.StagingItemReader;
import fr.training.springbatch.tools.staging.StagingItemWriter;
import fr.training.springbatch.tools.validator.JobParameterRequirementValidator;

/**
 * This pattern is a java configuration adaptation of the [Spring-batch
 * parallelJob.xml](https://github.com/spring-projects/spring-batch/blob/c4b001b732c8a4127e6a2a99e2fd00fff510f629/spring-batch-samples/src/main/resources/jobs/parallelJob.xml)
 * config.
 */
@Configuration
@ConditionalOnProperty(name = "spring.batch.job.names", havingValue = StagingJobConfig.STAGING_JOB)
public class StagingJobConfig extends AbstractJobConfiguration {

    protected static final String STAGING_JOB = "staging-job";

    @Autowired
    public DataSource dataSource;

    @Bean
    Job stagingJob(final Step stagingStep, final Step loadingStep, final JobRepository jobRepository) {
        return new JobBuilder(STAGING_JOB, jobRepository)
                .incrementer(new RunIdIncrementer())
                .validator(new JobParameterRequirementValidator("input-file", required().and(fileExist())))
                .start(stagingStep)
                .next(loadingStep)
                .listener(reportListener())
                .build();
    }

    @Bean
    Step stagingStep(final JobRepository jobRepository, final PlatformTransactionManager transactionManager,
            final ValidatingItemProcessor<Transaction> validatingProcessor,
            final ItemWriter<Transaction> stagingItemWriter, final ItemReader<Transaction> fileItemReader) {

        return new StepBuilder("staging-step", jobRepository)
                .<Transaction, Transaction> chunk(2, transactionManager)
                .reader(fileItemReader)
                .processor(validatingProcessor)
                .writer(stagingItemWriter)
                .listener(progressListener())
                .build();
    }

    @Bean
    Step loadingStep(final JobRepository jobRepository, final PlatformTransactionManager transactionManager,
            final ItemWriter<? super Transaction> transactionWriter) {

        return new StepBuilder("loading-step", jobRepository)
                .<ProcessIndicatorItemWrapper<Transaction>, Transaction> chunk(2, transactionManager)
                .reader(stagingReader())
                .processor(stagingProcessor())
                .writer(transactionWriter)
                .taskExecutor(taskExecutor())
                .listener(progressListener())
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

    @Bean
    ItemProcessor<? super ProcessIndicatorItemWrapper<Transaction>, ? extends Transaction> stagingProcessor() {
        final StagingItemProcessor<Transaction> itemProcessor = new StagingItemProcessor<Transaction>();
        itemProcessor.setDataSource(dataSource);
        return itemProcessor;
    }

    @StepScope // Mandatory for using jobParameters
    @Bean
    FlatFileItemReader<Transaction> fileItemReader(@Value("#{jobParameters['input-file']}") final String inputFile) {

        return new FlatFileItemReaderBuilder<Transaction>()
                .name("simpleImportReader")
                .resource(new FileSystemResource(inputFile))
                .delimited()
                .delimiter(";")
                .names("customerNumber", "number", "transactionDate", "amount")
                .linesToSkip(1)
                .fieldSetMapper(new BeanWrapperFieldSetMapper<Transaction>() {
                    {
                        setTargetType(Transaction.class);
                        setConversionService(localDateConverter());
                    }
                }).build();
    }

    @Bean("fixedValidator")
    SpringValidator<Transaction> getSpringValidator() {
        final SpringValidator<Transaction> validator = new SpringValidator<Transaction>();
        validator.setValidator(new TransactionValidator());
        return validator;
    }

    @Bean("processor")
    ValidatingItemProcessor<Transaction> validatingProcessor(@Qualifier("fixedValidator") final SpringValidator<Transaction> fixedValidator) {
        final ValidatingItemProcessor<Transaction> processor = new ValidatingItemProcessor<Transaction>(fixedValidator);
        return processor;
    }

    @Bean
    StagingItemWriter<Transaction> stagingItemWriter(final DataFieldMaxValueIncrementer StagingIncrementer) {
        final StagingItemWriter<Transaction> writer = new StagingItemWriter<Transaction>();
        writer.setDataSource(dataSource);
        writer.setIncrementer(StagingIncrementer);
        return writer;
    }

    @Bean
    DataFieldMaxValueIncrementer stagingIncrementer() throws MetaDataAccessException {
        final DataFieldMaxValueIncrementerFactory incrementerFactory = new DefaultDataFieldMaxValueIncrementerFactory(dataSource);
        return incrementerFactory.getIncrementer(DatabaseType.fromMetaData(dataSource).name(), "BATCH_STAGING_SEQ");
    }

    @Bean
    TaskExecutor taskExecutor() {
        return new SimpleAsyncTaskExecutor("spring_batch");
    }

    @Bean
    @DependsOnDatabaseInitialization
    JdbcBatchItemWriter<Transaction> transactionWriter() {

        return new JdbcBatchItemWriterBuilder<Transaction>() //
                .dataSource(dataSource)
                .sql("INSERT INTO Transaction(customer_number, number, transaction_date, amount) "
                        + "VALUES (:customerNumber, :number, :transactionDate, :amount )")
                .beanMapped() //
                .build();
    }

    @Bean
    ItemReader<? extends ProcessIndicatorItemWrapper<Transaction>> stagingReader() {
        final StagingItemReader<Transaction> reader = new StagingItemReader<Transaction>();
        reader.setDataSource(dataSource);
        return reader;
    }

}
