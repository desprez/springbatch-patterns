package fr.training.springbatch.job.synchrojob;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;

import fr.training.springbatch.app.dto.Customer;
import fr.training.springbatch.app.job.AbstractJobConfiguration;

/**
 * Synchronize 2 tables with full SQL implementation and produce a csv result
 * file.
 *
 * Note : No usage of ItemAccumulator
 *
 * @author Desprez
 */
public class SQLJoinSynchroJobConfig extends AbstractJobConfiguration {

	@Autowired
	private DataSource dataSource;

	/**
	 * @param sqlJoinSynchroStep the injected Step bean
	 * @return the job bean
	 */
	@Bean
	public Job sqlJoinSynchroJob(final Step sqlJoinSynchroStep) {

		return jobBuilderFactory.get("sqljoinsynchro-job") //
				.incrementer(new RunIdIncrementer()) // job can be launched as many times as desired
				.validator(new DefaultJobParametersValidator(new String[] { "output-file" }, new String[] {})) //
				.start(sqlJoinSynchroStep) //
				.listener(reportListener()) //
				.build();
	}

	@Bean
	public Step sqlJoinSynchroStep(final JdbcCursorItemReader<Customer> jdbcCustomerReader,
			final ItemWriter<Customer> customerWriter /* injected by Spring */) {

		return stepBuilderFactory.get("sqljoinsynchro-step") //
				.<Customer, Customer>chunk(10) //
				.reader(jdbcCustomerReader) //
				.writer(customerWriter) //
				.listener(reportListener()) //
				.build();
	}

	@Bean
	public JdbcCursorItemReader<Customer> jdbcCustomerReader() {
		return new JdbcCursorItemReaderBuilder<Customer>() //
				.dataSource(dataSource) //
				.name("customerReader") //
				.sql("SELECT c.NUMBER, c.ADDRESS, c.CITY, c.FIRST_NAME, c.LAST_NAME, c.POST_CODE, c.STATE, ROUND(SUM(t.AMOUNT),2) as BALANCE FROM CUSTOMER c "
						+ "LEFT JOIN TRANSACTION t ON c.NUMBER = t.CUSTOMER_NUMBER " //
						+ "GROUP BY c.NUMBER")
				.rowMapper((rs, rowNum) -> {
					final Customer customer = new Customer();
					customer.setNumber(rs.getString("NUMBER"));
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
	 * @param outputFile the injected output file job parameter
	 * @return a {@link FlatFileItemWriter} bean
	 */
	@StepScope // Mandatory for using jobParameters
	@Bean
	public FlatFileItemWriter<Customer> customerWriter(
			@Value("#{jobParameters['output-file']}") final String outputFile) {

		return new FlatFileItemWriterBuilder<Customer>().name("customerWriter")
				.resource(new FileSystemResource(outputFile)) //
				.delimited() //
				.delimiter(";") //
				.names(new String[] { "number", "firstName", "lastName", "address", "city", "state", "postCode",
				"balance" })
				.build();

	}
}
