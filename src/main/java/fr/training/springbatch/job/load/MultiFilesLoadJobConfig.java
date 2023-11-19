package fr.training.springbatch.job.load;

import static fr.training.springbatch.tools.validator.ParameterRequirement.required;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

import fr.training.springbatch.app.dto.Customer;
import fr.training.springbatch.app.job.AbstractJobConfiguration;
import fr.training.springbatch.tools.validator.JobParameterRequirementValidator;

/**
 * This Job load Transaction csv files present in a directory sequentialy insert each read line in a Transaction Table.
 *
 * @author desprez
 */
@Configuration
@ConditionalOnProperty(name = "spring.batch.job.names", havingValue = MultiFilesLoadJobConfig.MULTI_LOAD_JOB)
public class MultiFilesLoadJobConfig extends AbstractJobConfiguration {

    protected static final String MULTI_LOAD_JOB = "multi-load-job";

    @Value("${application.multi-load-step.chunksize:10}")
    private int chunkSize;

    @Autowired
    private DataSource dataSource;

    @Bean
    Job multiLoadJob(final Step multiLoadStep, final JobRepository jobRepository) {
        return new JobBuilder(MULTI_LOAD_JOB, jobRepository)
                .validator(new JobParameterRequirementValidator("input-path", required()))
                .start(multiLoadStep)
                .build();
    }

    @Bean
    Step multiLoadStep(final JobRepository jobRepository, final PlatformTransactionManager transactionManager,
            final MultiResourceItemReader<Customer> multiResourceItemReader, final JdbcBatchItemWriter<Customer> writer) {

        return new StepBuilder("multi-load-step", jobRepository)
                .<Customer, Customer> chunk(chunkSize, transactionManager)
                .reader(multiResourceItemReader)
                .writer(writer)
                .build();
    }

    @StepScope // Mandatory for using jobParameters
    @Bean
    MultiResourceItemReader<Customer> multiResourceItemReader(@Value("#{jobParameters['input-path']}") final Resource[] inputResources) {

        final MultiResourceItemReader<Customer> resourceItemReader = new MultiResourceItemReader<>();
        resourceItemReader.setResources(inputResources);
        resourceItemReader.setDelegate(reader());
        return resourceItemReader;
    }

    @Bean
    FlatFileItemReader<Customer> reader() {
        return new FlatFileItemReaderBuilder<Customer>()
                .name("itemReader")
                .delimited()
                .delimiter(";")
                .names("number", "firstName", "lastName", "address", "city", "state", "postCode", "birtDate") //
                .fieldSetMapper(new BeanWrapperFieldSetMapper<Customer>() {
                    {
                        setTargetType(Customer.class);
                        setConversionService(localDateConverter());
                    }
                }).build();
    }

    @Bean
    @DependsOnDatabaseInitialization
    JdbcBatchItemWriter<Customer> writer() {
        return new JdbcBatchItemWriterBuilder<Customer>()
                .dataSource(dataSource)
                .sql("INSERT INTO Customer(number, first_name, last_name, address, city, state, post_code, birth_date) "
                        + "VALUES (:number, :firstName, :lastName, :address, :city, :state, :postCode, :birthDate)")
                .beanMapped() //
                .build();
    }

}
