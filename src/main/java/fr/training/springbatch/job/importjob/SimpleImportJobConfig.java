package fr.training.springbatch.job.importjob;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
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
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.io.FileSystemResource;

import fr.training.springbatch.app.dto.Transaction;
import fr.training.springbatch.app.job.AbstractJobConfiguration;
import fr.training.springbatch.tools.listener.ItemCountListener;
import fr.training.springbatch.tools.tasklet.JdbcTasklet;

/**
 *
 */
public class SimpleImportJobConfig extends AbstractJobConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(SimpleImportJobConfig.class);

	@Value("${application.simple-import-step.chunksize:10}")
	private int chunkSize;

	@Autowired
	private DataSource dataSource;

	@Bean
	public Job simpleImportJob(final Step importStep) {
		return jobBuilderFactory.get("simple-import-job") //
				.incrementer(new RunIdIncrementer()) //
				.validator(new DefaultJobParametersValidator(new String[] { "input-file" }, new String[] {})) //
				.start(deleteStep()) //
				.next(importStep) //
				.listener(reportListener()) //
				.build();
	}

	/**
	 * Delete Step for deleting all previous records.
	 *
	 * @return the Step
	 */
	@Bean
	public Step deleteStep() {
		return stepBuilderFactory.get("delete-step") //
				.tasklet(deletePreviousRecordTasklet()) //
				.build();
	}

	private Tasklet deletePreviousRecordTasklet() {
		final JdbcTasklet deleteRecordTasklet = new JdbcTasklet();
		deleteRecordTasklet.setDataSource(dataSource);
		deleteRecordTasklet.setSql("DELETE FROM Transaction");
		return deleteRecordTasklet;
	}

	@Bean
	public Step importStep(final ItemReader<Transaction> importReader, //
			final ItemWriter<Transaction> importWriter) {

		return stepBuilderFactory.get("simple-import-step") //
				.<Transaction, Transaction>chunk(chunkSize) //
				.reader(importReader) //
				.processor(importProcessor()) //
				.writer(importWriter) //
				.listener(progressListener()) //
				.build();
	}

	/**
	 * Used for logging step progression
	 */
	@Override
	@Bean
	public ItemCountListener progressListener() {
		final ItemCountListener listener = new ItemCountListener();
		listener.setItemName("Transaction(s)");
		listener.setLoggingInterval(50); // Log process item count every 50
		return listener;
	}

	/**
	 * Fake processor that only logs
	 *
	 * @return an item processor
	 */
	private ItemProcessor<Transaction, Transaction> importProcessor() {
		return new ItemProcessor<Transaction, Transaction>() {

			@Override
			public Transaction process(final Transaction transaction) throws Exception {
				logger.debug("Processing {}", transaction);
				return transaction;
			}
		};
	}

	@StepScope // Mandatory for using jobParameters
	@Bean
	public FlatFileItemReader<Transaction> importReader(
			@Value("#{jobParameters['input-file']}") final String inputFile) {

		return new FlatFileItemReaderBuilder<Transaction>() //
				.name("simpleImportReader") //
				.resource(new FileSystemResource(inputFile)) //
				.delimited() //
				.delimiter(";") //
				.names(new String[] { "customerNumber", "number", "transactionDate", "amount" }) //
				.linesToSkip(1) //
				.fieldSetMapper(new BeanWrapperFieldSetMapper<Transaction>() {
					{
						setTargetType(Transaction.class);
						setConversionService(createConversionService());
					}
				}).build();
	}

	@Bean
	public JdbcBatchItemWriter<Transaction> importWriter() {

		return new JdbcBatchItemWriterBuilder<Transaction>() //
				.dataSource(dataSource)
				.sql("INSERT INTO Transaction(customer_number, number, transaction_date, amount) "
						+ "VALUES (:customerNumber, :number, :transactionDate, :amount )")
				// .beanMapped() //
				.itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<Transaction>()) //
				.build();
	}

	/**
	 * Converter to parse local date
	 */
	public ConversionService createConversionService() {
		final DefaultConversionService conversionService = new DefaultConversionService();
		DefaultConversionService.addDefaultConverters(conversionService);
		conversionService.addConverter(new Converter<String, LocalDate>() {
			@Override
			public LocalDate convert(final String text) {
				final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
				return LocalDate.parse(text, formatter);
			}
		});
		return conversionService;
	}
}
