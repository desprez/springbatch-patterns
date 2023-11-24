package fr.training.springbatch.job.partition.jdbc;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import fr.training.springbatch.app.dto.Customer;
import fr.training.springbatch.app.job.AbstractJobConfiguration;

/**
 * <b>Pattern #15</b>
 *
 * @author Desprez
 */
@Configuration
@ConditionalOnProperty(name = "spring.batch.job.names", havingValue = JDBCPartitionJobConfig.PARTITION_JOB)
public class JDBCPartitionJobConfig extends AbstractJobConfiguration {

    protected static final String PARTITION_JOB = "partition-job";

    @Autowired
    private DataSource dataSource;

    @Bean
    Job partitionJob(final Step masterStep, final JobRepository jobRepository) {

        return new JobBuilder(PARTITION_JOB, jobRepository) //
                .start(masterStep)//
                .build();
    }

    // Master
    @Bean
    Step masterStep(final JobRepository jobRepository, final Step slaveStep) {

        return new StepBuilder("master-step", jobRepository)
                .partitioner(slaveStep.getName(), partitioner())
                .step(slaveStep)
                .gridSize(4)
                .taskExecutor(new SimpleAsyncTaskExecutor())
                .build();
    }

    // slave step
    @Bean
    Step slaveStep(final JobRepository jobRepository, final PlatformTransactionManager transactionManager, final ItemReader<Customer> pagingItemReader,
            final JdbcBatchItemWriter<Customer> customerItemWriter) {

        return new StepBuilder("slave-step", jobRepository)
                .<Customer, Customer> chunk(1000, transactionManager)
                .reader(pagingItemReader)
                .writer(customerItemWriter)
                .build();
    }

    @Bean
    @DependsOnDatabaseInitialization
    ColumnRangePartitioner partitioner() {
        final ColumnRangePartitioner columnRangePartitioner = new ColumnRangePartitioner();
        columnRangePartitioner.setColumn("number");
        columnRangePartitioner.setDataSource(dataSource);
        columnRangePartitioner.setTable("customer");
        return columnRangePartitioner;
    }

    @StepScope // Mandatory for using jobParameters
    @Bean
    @DependsOnDatabaseInitialization
    JdbcPagingItemReader<Customer> pagingItemReader(@Value("#{stepExecutionContext['minValue']}") final Long minValue,
            @Value("#{stepExecutionContext['maxValue']}") final Long maxValue) {
        System.out.println("reading " + minValue + " to " + maxValue);

        final Map<String, Order> sortKeys = new HashMap<>();
        sortKeys.put("number", Order.ASCENDING);

        final MySqlPagingQueryProvider queryProvider = new MySqlPagingQueryProvider();
        queryProvider.setSelectClause("number, first_name, last_name, address, city, post_code, state");
        queryProvider.setFromClause("from customer");
        queryProvider.setWhereClause("where number >= " + minValue + " and number < " + maxValue);
        queryProvider.setSortKeys(sortKeys);

        final JdbcPagingItemReader<Customer> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(dataSource);
        reader.setFetchSize(1000);
        reader.setRowMapper((rs, rowNum) -> {
            final Customer customer = new Customer();
            customer.setNumber(rs.getLong("NUMBER"));
            customer.setFirstName(rs.getString("FIRST_NAME"));
            customer.setLastName(rs.getString("LAST_NAME"));
            customer.setAddress(rs.getString("ADDRESS"));
            customer.setCity(rs.getString("CITY"));
            customer.setPostCode(rs.getString("POST_CODE"));
            customer.setState(rs.getString("STATE"));
            return customer;
        });
        reader.setQueryProvider(queryProvider);

        return reader;
    }

    @Bean
    @DependsOnDatabaseInitialization
    JdbcBatchItemWriter<Customer> customerItemWriter() {

        return new JdbcBatchItemWriterBuilder<Customer>()
                .dataSource(dataSource)
                .sql("INSERT INTO new_customer( first_name, last_name, address, city, post_code, state, number) VALUES (:firstName, :lastName, :address, :city, :postCode, :state, :number)")
                .beanMapped()
                .build();
    }

}