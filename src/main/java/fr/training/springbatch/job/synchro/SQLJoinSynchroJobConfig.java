package fr.training.springbatch.job.synchro;

import static fr.training.springbatch.tools.validator.ParameterRequirement.fileWritable;
import static fr.training.springbatch.tools.validator.ParameterRequirement.required;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import fr.training.springbatch.app.dto.Customer;
import fr.training.springbatch.app.job.AbstractJobConfiguration;
import fr.training.springbatch.tools.validator.JobParameterRequirementValidator;

/**
 * <b>Pattern #7</b> Synchronize 2 tables with full SQL implementation and produce a csv result file. <br>
 * <b>Note :</b> No usage of ItemAccumulator
 *
 * @author Desprez
 */
@Configuration
@ConditionalOnProperty(name = "spring.batch.job.names", havingValue = SQLJoinSynchroJobConfig.SQL_JOIN_SYNCHRO_JOB)
public class SQLJoinSynchroJobConfig extends AbstractJobConfiguration {

    protected static final String SQL_JOIN_SYNCHRO_JOB = "sqljoinsynchro-job";

    @Autowired
    private DataSource dataSource;

    @Bean
    Job sqlJoinSynchroJob(final Step sqlJoinSynchroStep, final JobRepository jobRepository) {

        return new JobBuilder(SQL_JOIN_SYNCHRO_JOB, jobRepository)
                .incrementer(new RunIdIncrementer()) // job can be launched as many times as desired
                .validator(new JobParameterRequirementValidator("output-file", required().and(fileWritable())))
                .start(sqlJoinSynchroStep)
                .listener(reportListener())
                .build();
    }

    @Bean
    Step sqlJoinSynchroStep(final JobRepository jobRepository, final PlatformTransactionManager transactionManager,
            final JdbcCursorItemReader<Customer> jdbcCustomerReader,
            final ItemWriter<Customer> customerWriter /* injected by Spring */) {

        return new StepBuilder("sqljoinsynchro-step", jobRepository)
                .<Customer, Customer> chunk(10, transactionManager)
                .reader(jdbcCustomerReader)
                .writer(customerWriter)
                .listener(reportListener())
                .build();
    }

    @Bean
    JdbcCursorItemReader<Customer> jdbcCustomerReader() {
        return new JdbcCursorItemReaderBuilder<Customer>()
                .dataSource(dataSource)
                .name("customerReader")
                .sql("SELECT c.NUMBER, c.ADDRESS, c.CITY, c.FIRST_NAME, c.LAST_NAME, c.POST_CODE, c.STATE, ROUND(SUM(t.AMOUNT),2) as BALANCE FROM CUSTOMER c "
                        + "LEFT JOIN TRANSACTION t ON c.NUMBER = t.CUSTOMER_NUMBER "
                        + "GROUP BY c.NUMBER")
                .rowMapper((rs, rowNum) -> {
                    final Customer customer = new Customer();
                    customer.setNumber(rs.getLong("NUMBER"));
                    customer.setAddress(rs.getString("ADDRESS"));
                    customer.setCity(rs.getString("CITY"));
                    customer.setFirstName(rs.getString("FIRST_NAME"));
                    customer.setLastName(rs.getString("LAST_NAME"));
                    customer.setPostCode(rs.getString("POST_CODE"));
                    customer.setState(rs.getString("STATE"));
                    customer.setBalance(rs.getDouble("BALANCE"));
                    return customer;
                }).build();
    }

    /**
     * @param outputFile
     *            the injected output file job parameter
     * @return a {@link FlatFileItemWriter} bean
     */
    @StepScope // Mandatory for using jobParameters
    @Bean
    FlatFileItemWriter<Customer> customerWriter(@Value("#{jobParameters['output-file']}") final String outputFile) {

        return new FlatFileItemWriterBuilder<Customer>()
                .name("customerWriter")
                .resource(new FileSystemResource(outputFile))
                .delimited()
                .delimiter(";")
                .names("number", "firstName", "lastName", "address", "city", "state", "postCode", "balance") //
                .build();

    }
}
