package fr.training.springbatch.job.multilinesrecord;

import static fr.training.springbatch.tools.validator.ParameterRequirement.fileWritable;
import static fr.training.springbatch.tools.validator.ParameterRequirement.required;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.PassThroughLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import fr.training.springbatch.app.dto.Customer;
import fr.training.springbatch.app.dto.Transaction;
import fr.training.springbatch.app.job.AbstractJobConfiguration;
import fr.training.springbatch.tools.validator.JobParameterRequirementValidator;

/**
 * <b>Pattern #18</b> This job allows you to write records of different types from 2 tables to a file. The records from the master table are read by the
 * {@link JdbcCursorItemReader} and the {@link ItemProcessor} complete with the records from the detail table. A Custom {@link ItemWriter} is responsible for
 * writing the aggregated data.
 *
 * @author Desprez
 */
@Configuration
@ConditionalOnProperty(name = "spring.batch.job.names", havingValue = MultiLinesExtractJobConfig.MULTILINES_EXTRACT_JOB)
public class MultiLinesExtractJobConfig extends AbstractJobConfiguration {

    protected static final String MULTILINES_EXTRACT_JOB = "multilines-extract-job";

    @Value("${application.multilines-extract-step.chunksize:10}")
    private int chunkSize;

    @Autowired
    public DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Bean
    Job multilinesExtractJob(final Step multilinesExtractStep, final JobRepository jobRepository) {

        return new JobBuilder(MULTILINES_EXTRACT_JOB, jobRepository)
                .validator(new JobParameterRequirementValidator("output-file", required().and(fileWritable())))
                .incrementer(new RunIdIncrementer())
                .flow(multilinesExtractStep)
                .end()
                .listener(reportListener())
                .build();
    }

    @Bean
    Step multilinesExtractStep(final JobRepository jobRepository, final PlatformTransactionManager transactionManager,
            final MultiLineCustomerItemWriter multilinesExtractWriter) {

        return new StepBuilder("multilines-extract-step", jobRepository)
                .<Customer, Customer> chunk(chunkSize, transactionManager)
                .reader(customerJDBCReader())
                .processor(multilinesExtractProcessor())
                .writer(multilinesExtractWriter)
                .listener(progressListener())
                .build();
    }

    @Bean
    JdbcCursorItemReader<Customer> customerJDBCReader() {

        return new JdbcCursorItemReaderBuilder<Customer>()
                .name("customerJDBCReader")
                .dataSource(dataSource)
                .sql("SELECT number, first_name, last_name, address, city, state, post_code, birth_date FROM Customer ORDER BY number ASC")
                .rowMapper(new BeanPropertyRowMapper<>(Customer.class))
                .build();
    }

    private ItemProcessor<Customer, Customer> multilinesExtractProcessor() {
        return item -> {
            final List<Transaction> transactions = getTransactionForCustomer(item.getNumber());
            return new Customer(item, transactions);
        };
    }

    private List<Transaction> getTransactionForCustomer(final Long number) {
        return jdbcTemplate.query("SELECT customer_number, number, amount, transaction_date FROM Transaction WHERE customer_number = ?",
                (rs, row) -> new Transaction(
                        rs.getLong("customer_number"),
                        rs.getString("number"),
                        rs.getTimestamp("transaction_date").toLocalDateTime().toLocalDate(),
                        rs.getDouble("amount")),
                new Object[] { number });
    }

    @Bean
    MultiLineCustomerItemWriter multilinesExtractWriter(final FlatFileItemWriter<String> fileItemWriter) {
        final MultiLineCustomerItemWriter writer = new MultiLineCustomerItemWriter();
        writer.setDelegate(fileItemWriter);
        return writer;
    }

    @StepScope // Mandatory for using jobParameters
    @Bean
    FlatFileItemWriter<String> fileItemWriter(@Value("#{jobParameters['output-file']}") final String outputFile) {
        return new FlatFileItemWriterBuilder<String>()
                .name("fileItemWriter")
                .resource(new FileSystemResource(outputFile))
                .lineAggregator(new PassThroughLineAggregator<>())
                .build();
    }

}