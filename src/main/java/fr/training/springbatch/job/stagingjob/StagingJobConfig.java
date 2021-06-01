package fr.training.springbatch.job.stagingjob;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.support.DataFieldMaxValueIncrementerFactory;
import org.springframework.batch.item.database.support.DefaultDataFieldMaxValueIncrementerFactory;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.validator.SpringValidator;
import org.springframework.batch.item.validator.ValidatingItemProcessor;
import org.springframework.batch.support.DatabaseType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.support.MetaDataAccessException;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;

import fr.training.springbatch.app.dto.Transaction;
import fr.training.springbatch.app.job.AbstractJobConfiguration;
import fr.training.springbatch.tools.listener.ItemCountListener;
import fr.training.springbatch.tools.staging.ProcessIndicatorItemWrapper;
import fr.training.springbatch.tools.staging.StagingItemProcessor;
import fr.training.springbatch.tools.staging.StagingItemReader;
import fr.training.springbatch.tools.staging.StagingItemWriter;

/**
 * This pattern is a java configuration adaptation of the [Spring-batch
 * parallelJob.xml](https://github.com/spring-projects/spring-batch/blob/c4b001b732c8a4127e6a2a99e2fd00fff510f629/spring-batch-samples/src/main/resources/jobs/parallelJob.xml)
 * config.
 */
public class StagingJobConfig extends AbstractJobConfiguration {

	@Autowired
	public DataSource dataSource;

	@Bean
	public Job stagingJob(final Step stagingStep, final Step loadingStep) {
		return jobBuilderFactory.get("staging-job") //
				.incrementer(new RunIdIncrementer()) //
				.validator(new DefaultJobParametersValidator(new String[] { "input-file" }, new String[] {})) //
				.start(stagingStep) //
				.next(loadingStep) //
				.listener(reportListener()) //
				.build();
	}

	@Bean
	public Step stagingStep(final ValidatingItemProcessor<Transaction> validatingProcessor, //
			final ItemWriter<Transaction> stagingItemWriter, final ItemReader<Transaction> fileItemReader) {

		return stepBuilderFactory.get("staging-step") //
				.<Transaction, Transaction>chunk(2) //
				.reader(fileItemReader) //
				.processor(validatingProcessor)//
				.writer(stagingItemWriter) //
				.listener(progressListener()) //
				.build();
	}

	@Bean
	public Step loadingStep(final ItemWriter<? super Transaction> transactionWriter) {

		return stepBuilderFactory.get("loading-step") //
				.<ProcessIndicatorItemWrapper<Transaction>, Transaction>chunk(2) //
				.reader(stagingReader()) //
				.processor(stagingProcessor())//
				.writer(transactionWriter) //
				.taskExecutor(taskExecutor()) //
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

	@Bean
	public ItemProcessor<? super ProcessIndicatorItemWrapper<Transaction>, ? extends Transaction> stagingProcessor() {
		final StagingItemProcessor<Transaction> itemProcessor = new StagingItemProcessor<Transaction>();
		itemProcessor.setDataSource(dataSource);
		return itemProcessor;
	}

	@StepScope // Mandatory for using jobParameters
	@Bean
	public FlatFileItemReader<Transaction> fileItemReader(
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
						setConversionService(localDateConverter());
					}
				}).build();
	}

	@Bean("fixedValidator")
	public SpringValidator<Transaction> getSpringValidator() {
		final SpringValidator<Transaction> validator = new SpringValidator<Transaction>();
		validator.setValidator(new TransactionValidator());
		return validator;
	}

	@Bean("processor")
	public ValidatingItemProcessor<Transaction> validatingProcessor(
			@Qualifier("fixedValidator") final SpringValidator<Transaction> fixedValidator) {
		final ValidatingItemProcessor<Transaction> processor = new ValidatingItemProcessor<Transaction>(fixedValidator);
		return processor;
	}

	@Bean
	public StagingItemWriter<Transaction> stagingItemWriter(final DataFieldMaxValueIncrementer StagingIncrementer) {
		final StagingItemWriter<Transaction> writer = new StagingItemWriter<Transaction>();
		writer.setDataSource(dataSource);
		writer.setIncrementer(StagingIncrementer);
		return writer;
	}

	@Bean
	public DataFieldMaxValueIncrementer stagingIncrementer() throws MetaDataAccessException {
		final DataFieldMaxValueIncrementerFactory incrementerFactory = new DefaultDataFieldMaxValueIncrementerFactory(
				dataSource);
		return incrementerFactory.getIncrementer(DatabaseType.fromMetaData(dataSource).name(), "BATCH_STAGING_SEQ");
	}

	@Bean
	public TaskExecutor taskExecutor() {
		return new SimpleAsyncTaskExecutor("spring_batch");
	}

	@Bean
	public JdbcBatchItemWriter<Transaction> transactionWriter() {

		return new JdbcBatchItemWriterBuilder<Transaction>() //
				.dataSource(dataSource)
				.sql("INSERT INTO Transaction(customer_number, number, transaction_date, amount) "
						+ "VALUES (:customerNumber, :number, :transactionDate, :amount )")
				.beanMapped() //
				.build();
	}

	@Bean
	public ItemReader<? extends ProcessIndicatorItemWrapper<Transaction>> stagingReader() {
		final StagingItemReader<Transaction> reader = new StagingItemReader<Transaction>();
		reader.setDataSource(dataSource);
		return reader;
	}

}
