package fr.training.springbatch.job.multilinesrecord;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.PassThroughLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import fr.training.springbatch.app.dto.Customer;
import fr.training.springbatch.app.dto.Transaction;
import fr.training.springbatch.app.job.AbstractJobConfiguration;

public class MultiLinesExtractJobConfig extends AbstractJobConfiguration {

	@Value("${application.multilines-extract-step.chunksize:10}")
	private int chunkSize;

	@Autowired
	public DataSource dataSource;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Bean
	public Job multilinesExtractJob(final Step multilinesExtractStep) {
		return jobBuilderFactory.get("multilines-extract-job") //
				.validator(new DefaultJobParametersValidator(new String[] { "output-file" }, new String[] {}))
				.incrementer(new RunIdIncrementer()) //
				.flow(multilinesExtractStep) //
				.end() //
				.listener(reportListener()) //
				.build();
	}

	@Bean
	public Step multilinesExtractStep(final MultiLineCustomerItemWriter multilinesExtractWriter) {
		return stepBuilderFactory.get("multilines-extract-step") //
				.<Customer, Customer>chunk(chunkSize) //
				.reader(customerJDBCReader()) //
				.processor(multilinesExtractProcessor()) //
				.writer(multilinesExtractWriter) //
				.listener(progressListener()) //
				.build();
	}

	@Bean
	public JdbcCursorItemReader<Customer> customerJDBCReader() {

		return new JdbcCursorItemReaderBuilder<Customer>() //
				.name("customerJDBCReader") //
				.dataSource(dataSource) //
				.sql("SELECT number, first_name, last_name, address, city, state, post_code, birth_date FROM Customer ORDER BY number ASC") //
				.rowMapper(new BeanPropertyRowMapper<>(Customer.class)) //
				.build();
	}

	private ItemProcessor<Customer, Customer> multilinesExtractProcessor() {
		return new ItemProcessor<Customer, Customer>() {
			@Override
			public Customer process(final Customer item) throws Exception {
				final List<Transaction> transactions = getTransactionForCustomer(item.getNumber());
				return new Customer(item, transactions);
			}

		};
	}

	private List<Transaction> getTransactionForCustomer(final Long number) {
		return jdbcTemplate.query(
				"SELECT customer_number, number, amount, transaction_date"
						+ " FROM Transaction WHERE customer_number = ?",
						new Object[] { number }, (rs, row) -> new Transaction( //
								rs.getLong("customer_number"), //
								rs.getString("number"), //
								rs.getTimestamp("transaction_date").toLocalDateTime().toLocalDate(), //
								rs.getDouble("amount")));
	}

	@Bean
	public MultiLineCustomerItemWriter multilinesExtractWriter(final FlatFileItemWriter<String> fileItemWriter) {
		final MultiLineCustomerItemWriter writer = new MultiLineCustomerItemWriter();
		writer.setDelegate(fileItemWriter);
		return writer;
	}

	@StepScope // Mandatory for using jobParameters
	@Bean
	public FlatFileItemWriter<String> fileItemWriter(
			@Value("#{jobParameters['output-file']}") final String outputFile) {
		return new FlatFileItemWriterBuilder<String>() //
				.name("fileItemWriter") //
				.resource(new FileSystemResource(outputFile)) //
				.lineAggregator(new PassThroughLineAggregator<String>())
				.build();
	}

}
