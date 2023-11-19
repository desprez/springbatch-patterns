package fr.training.springbatch.job.update;

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
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import fr.training.springbatch.app.dto.Customer;
import fr.training.springbatch.app.job.AbstractJobConfiguration;
import fr.training.springbatch.tools.listener.ItemCountListener;
import fr.training.springbatch.tools.listener.RejectFileSkipListener;
import fr.training.springbatch.tools.validator.AdditiveJobParametersValidatorBuilder;
import fr.training.springbatch.tools.validator.JobParameterRequirementValidator;

/**
 *
 */
@Configuration
@ConditionalOnProperty(name = "spring.batch.job.names", havingValue = SimpleUpdateJobConfig.SIMPLE_UPDATE_JOB)
public class SimpleUpdateJobConfig extends AbstractJobConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(SimpleUpdateJobConfig.class);

    protected static final String SIMPLE_UPDATE_JOB = "simple-update-job";

    @Value("${application.simple-update-step.chunksize:10}")
    private int chunkSize;

    @Autowired
    private DataSource dataSource;

    @Bean
    Job simpleImportJob(final Step updateStep, final JobRepository jobRepository) {
        return new JobBuilder(SIMPLE_UPDATE_JOB, jobRepository)
                .incrementer(new RunIdIncrementer())
                .validator(new AdditiveJobParametersValidatorBuilder()
                        .addValidator(new JobParameterRequirementValidator("input-file", required().and(fileExist())))
                        .addValidator(new JobParameterRequirementValidator("rejectfile", required()))
                        .build())
                .start(updateStep)
                .listener(reportListener())
                .build();
    }

    @Bean
    Step updateStep(final JobRepository jobRepository, final PlatformTransactionManager transactionManager, final ItemReader<Customer> updateReader, //
            final ItemWriter<Customer> updateWriter, final RejectFileSkipListener<Customer, Customer> rejectListener) {

        return new StepBuilder("simple-update-step", jobRepository)
                .<Customer, Customer> chunk(chunkSize, transactionManager)
                .reader(updateReader)
                .processor(updateProcessor())
                .writer(updateWriter)
                .faultTolerant()
                // .skipPolicy(new AlwaysSkipItemSkipPolicy())
                .skipLimit(100)
                .skip(RuntimeException.class).listener(progressListener())
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
        listener.setItemName("Customer(s)");
        listener.setLoggingInterval(50); // Log process item count every 50
        return listener;
    }

    /**
     * Fake processor that only logs
     *
     * @return an item processor
     */
    private ItemProcessor<Customer, Customer> updateProcessor() {
        return customer -> {
            logger.debug("Processing {}", customer);
            return customer;
        };
    }

    @StepScope // Mandatory for using jobParameters
    @Bean
    FlatFileItemReader<Customer> updateReader(@Value("#{jobParameters['input-file']}") final String inputFile) {

        return new FlatFileItemReaderBuilder<Customer>()
                .name("simpleUpdateReader")
                .resource(new FileSystemResource(inputFile))
                .delimited()
                .delimiter(";")
                .names("number", "birthDate")
                .linesToSkip(1) //
                .fieldSetMapper(new BeanWrapperFieldSetMapper<Customer>() {
                    {
                        setTargetType(Customer.class);
                        setConversionService(localDateConverter());
                    }
                }).build();
    }

    @Bean
    @DependsOnDatabaseInitialization
    JdbcBatchItemWriter<Customer> updateWriter() {

        return new JdbcBatchItemWriterBuilder<Customer>()
                .dataSource(dataSource)
                .sql("UPDATE Customer SET birth_date = :birthDate WHERE number = :number")
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .build();
    }

    @StepScope // Mandatory for using jobParameters
    @Bean
    RejectFileSkipListener<Customer, Customer> rejectListener(@Value("#{jobParameters['rejectfile']}") final String rejectfile) throws IOException {
        return new RejectFileSkipListener<>(new File(rejectfile));
    }

}