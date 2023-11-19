package fr.training.springbatch.job.multidestinationjob;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.function.FunctionItemProcessor;
import org.springframework.batch.item.support.ClassifierCompositeItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.transaction.PlatformTransactionManager;

import fr.training.springbatch.app.dto.Customer;
import fr.training.springbatch.app.job.AbstractJobConfiguration;

/**
 * This job use a custom {@link org.springframework.classify.Classifier} to distinguish customers and {@link ClassifierCompositeItemWriter} to route items to
 * the accoring itemWriter.
 */
@Configuration
@ConditionalOnProperty(name = "spring.batch.job.names", havingValue = MultiDestinationJobConfig.MULTI_DESTINATION_JOB)
public class MultiDestinationJobConfig extends AbstractJobConfiguration {

    protected static final String MULTI_DESTINATION_JOB = "multi-destination-job";

    @Autowired
    private DataSource dataSource;

    @Bean
    Job multiDestinationJob(final Step multiDestinationStep, final JobRepository jobRepository) {
        return new JobBuilder(MULTI_DESTINATION_JOB, jobRepository)
                .start(multiDestinationStep)
                .build();
    }

    @Bean
    Step multiDestinationStep(final JobRepository jobRepository, final PlatformTransactionManager transactionManager,
            final ItemWriter<Customer> classifierCustomerCompositeItemWriter, final FlatFileItemWriter<Customer> after50Writer,
            final FlatFileItemWriter<Customer> before50Writer) throws Exception {

        return new StepBuilder("multi-destination-step", jobRepository)
                .<Customer, Customer> chunk(10, transactionManager)
                .reader(customerJDBCReader())
                .processor(new FunctionItemProcessor<>(c -> {
                    // just compute age of the customer
                    c.computeAge();
                    return c;
                })) //
                .writer(classifierCustomerCompositeItemWriter)
                .stream(after50Writer)
                .stream(before50Writer)
                .build();
    }

    @Bean
    JdbcCursorItemReader<Customer> customerJDBCReader() {

        return new JdbcCursorItemReaderBuilder<Customer>()
                .name("customerJDBCReader")
                .dataSource(dataSource)
                .sql("SELECT number, first_name, last_name, address, city, state, post_code, birth_date FROM Customer ORDER BY number ASC") //
                .rowMapper(new BeanPropertyRowMapper<>(Customer.class))
                .build();
    }

    @Bean
    ClassifierCompositeItemWriter<Customer> classifierCustomerCompositeItemWriter(final ItemWriter<Customer> after50Writer,
            final ItemWriter<Customer> before50Writer) throws Exception {

        final ClassifierCompositeItemWriter<Customer> compositeItemWriter = new ClassifierCompositeItemWriter<>();
        compositeItemWriter.setClassifier(new CustomerClassifier(after50Writer, before50Writer));
        return compositeItemWriter;
    }

    @StepScope // Mandatory for using jobParameters
    @Bean
    FlatFileItemWriter<Customer> after50Writer(@Value("#{jobParameters['outputFile1']}") final String outputFile1) {

        return new FlatFileItemWriterBuilder<Customer>()
                .name("itemWriter")
                .resource(new FileSystemResource(outputFile1))
                .delimited()
                .delimiter(";")
                .names("number", "firstName", "lastName", "address", "city", "postCode", "state", "age")
                .build();
    }

    @StepScope // Mandatory for using jobParameters
    @Bean
    FlatFileItemWriter<Customer> before50Writer(@Value("#{jobParameters['outputFile2']}") final String outputFile2) {
        return new FlatFileItemWriterBuilder<Customer>()
                .name("itemWriter")
                .resource(new FileSystemResource(outputFile2))
                .delimited()
                .delimiter(";")
                .names("number", "firstName", "lastName", "address", "city", "postCode", "state", "age")
                .build();
    }

}
