package fr.training.springbatch.job.partition.jdbc;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import fr.training.springbatch.app.dto.Customer;
import fr.training.springbatch.app.job.AbstractJobConfiguration;

/**
 *
 */
public class JDBCPartitionJobConfig extends AbstractJobConfiguration {

	@Autowired
	private DataSource dataSource;

	@Bean
	public Job partitionJob(final Step masterStep) {

		return jobBuilderFactory.get("partition-job") //
				.start(masterStep)//
				.build();
	}

	// Master
	@Bean
	public Step masterStep(final Step slaveStep) {

		return stepBuilderFactory.get("master-step") //
				.partitioner(slaveStep.getName(), partitioner()) //
				.step(slaveStep) //
				.gridSize(4) //
				.taskExecutor(new SimpleAsyncTaskExecutor()) //
				.build();
	}

	// slave step
	@Bean
	public Step slaveStep(final ItemReader<Customer> pagingItemReader, final JdbcBatchItemWriter<Customer> customerItemWriter) {

		return stepBuilderFactory.get("slave-step") //
				.<Customer, Customer>chunk(1000) //
				.reader(pagingItemReader) //
				.writer(customerItemWriter) //
				.build();
	}

	@Bean
	public ColumnRangePartitioner partitioner() {
		final ColumnRangePartitioner columnRangePartitioner = new ColumnRangePartitioner();
		columnRangePartitioner.setColumn("number");
		columnRangePartitioner.setDataSource(dataSource);
		columnRangePartitioner.setTable("customer");
		return columnRangePartitioner;
	}

	@StepScope // Mandatory for using jobParameters
	@Bean
	public JdbcPagingItemReader<Customer> pagingItemReader(
			@Value("#{stepExecutionContext['minValue']}") final Long minValue,
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
	public JdbcBatchItemWriter<Customer> customerItemWriter() {
		return new JdbcBatchItemWriterBuilder<Customer>() //
				.dataSource(dataSource)
				.sql("INSERT INTO new_customer( first_name, last_name, address, city, post_code, state, number) VALUES (:firstName, :lastName, :address, :city, :postCode, :state, :number)")
				.beanMapped() //
				.build();

	}

}