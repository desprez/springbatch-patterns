package fr.training.springbatch.job.multidestinationjob;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.function.FunctionItemProcessor;
import org.springframework.batch.item.support.ClassifierCompositeItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import fr.training.springbatch.app.dto.Customer;
import fr.training.springbatch.app.job.AbstractJobConfiguration;

/**
 * This job use a custom {@link org.springframework.classify.Classifier} to distinguish customers and
 * {@link ClassifierCompositeItemWriter} to route items to the accoring itemWriter.
 */
public class MultiDestinationJobConfig extends AbstractJobConfiguration {

	@Autowired
	private DataSource dataSource;

	@Bean
	public Job multiDestinationJob(final Step multiDestinationStep) {
		return jobBuilderFactory.get("multi-destination-job") //
				.start(multiDestinationStep) //
				.build();
	}

	@Bean
	public Step multiDestinationStep(final ItemWriter<Customer> classifierCustomerCompositeItemWriter,
			final FlatFileItemWriter<Customer> after50Writer, final FlatFileItemWriter<Customer> before50Writer)
					throws Exception {

		return stepBuilderFactory.get("multi-destination-step") //
				.<Customer, Customer>chunk(10) //
				.reader(customerJDBCReader()) //
				.processor(new FunctionItemProcessor<Customer, Customer>(c -> {
					// just compute age of the customer
					c.computeAge();
					return c;
				})) //
				.writer(classifierCustomerCompositeItemWriter) //
				.stream(after50Writer) //
				.stream(before50Writer) //
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

	@Bean
	public ClassifierCompositeItemWriter<Customer> classifierCustomerCompositeItemWriter(
			final ItemWriter<Customer> after50Writer, final ItemWriter<Customer> before50Writer) throws Exception {

		final ClassifierCompositeItemWriter<Customer> compositeItemWriter = new ClassifierCompositeItemWriter<>();
		compositeItemWriter.setClassifier(new CustomerClassifier(after50Writer, before50Writer));
		return compositeItemWriter;
	}

	@StepScope // Mandatory for using jobParameters
	@Bean
	public FlatFileItemWriter<Customer> after50Writer(
			@Value("#{jobParameters['outputFile1']}") final String outputFile1) {

		return new FlatFileItemWriterBuilder<Customer>() //
				.name("itemWriter") //
				.resource(new FileSystemResource(outputFile1)) //
				.delimited() //
				.delimiter(";") //
				.names("number", "firstName", "lastName", "address", "city", "postCode", "state", "age") //
				.build();
	}

	@StepScope // Mandatory for using jobParameters
	@Bean
	public FlatFileItemWriter<Customer> before50Writer(
			@Value("#{jobParameters['outputFile2']}") final String outputFile2) {
		return new FlatFileItemWriterBuilder<Customer>() //
				.name("itemWriter") //
				.resource(new FileSystemResource(outputFile2)) //
				.delimited() //
				.delimiter(";") //
				.names("number", "firstName", "lastName", "address", "city", "postCode", "state", "age") //
				.build();
	}

}
