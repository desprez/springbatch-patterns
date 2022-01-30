package fr.training.springbatch.job.partition.file;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.partition.support.MultiResourcePartitioner;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import fr.training.springbatch.app.dto.Customer;
import fr.training.springbatch.app.job.AbstractJobConfiguration;
import fr.training.springbatch.tools.listener.OutputFileListener;

/**
 *
 * @author desprez
 */
public class FilePartitionJobConfig extends AbstractJobConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(FilePartitionJobConfig.class);

	public static final BigDecimal FIXED_AMOUNT = new BigDecimal("5");

	@Bean
	public Job partitionJob(final Step masterStep) {

		return jobBuilderFactory.get("partition-job") //
				.validator(new DefaultJobParametersValidator(new String[] { "output-path" }, new String[] {}))
				.start(masterStep)//
				.build();
	}

	// Master
	@Bean
	public Step masterStep(final Step slaveStep, final MultiResourcePartitioner partitioner) throws IOException {

		return stepBuilderFactory.get("master-step") //
				.partitioner(slaveStep.getName(), partitioner) //
				.step(slaveStep) //
				.gridSize(4) //
				.taskExecutor(new SimpleAsyncTaskExecutor()) //
				.build();
	}

	@StepScope // Mandatory for using jobParameters
	@Bean
	public MultiResourcePartitioner partitioner(
			@Value("#{jobParameters['input-path']}") final Resource[] inputResources) throws IOException {
		final MultiResourcePartitioner partitioner = new MultiResourcePartitioner();
		partitioner.partition(10);
		partitioner.setResources(inputResources);
		return partitioner;
	}

	// slave step
	@Bean
	public Step slaveStep(final FlatFileItemReader<Customer> itemReader, //
			final ItemProcessor<Customer, Customer> processor, //
			final FlatFileItemWriter<Customer> itemWriter, //
			final OutputFileListener fileNameListener) {

		return stepBuilderFactory.get("slave-step") //
				.<Customer, Customer>chunk(10) //
				.reader(itemReader) //
				.processor(processor) //
				.writer(itemWriter) //
				.listener(fileNameListener).build();
	}

	@StepScope // Mandatory for using jobParameters
	@Bean
	public OutputFileListener fileNameListener(@Value("#{jobParameters['output-path']}") final String outputPath) {
		final OutputFileListener listener = new OutputFileListener();
		listener.setPath(outputPath);
		return listener;
	}

	@StepScope // Mandatory for using jobParameters
	@Bean
	public FlatFileItemReader<Customer> itemReader(@Value("#{stepExecutionContext['fileName']}") final String fileName)
			throws MalformedURLException {
		logger.info("fileName {}", fileName);

		return new FlatFileItemReaderBuilder<Customer>() //
				.name("itemReader") //
				.resource(new UrlResource(fileName)) //
				.delimited() //
				.delimiter(";") //
				.names("number", "firstName", "lastName", "address", "city", "postCode", "state", "birthDate") //
				// .linesToSkip(1) //
				.fieldSetMapper(new BeanWrapperFieldSetMapper<Customer>() {
					{
						setTargetType(Customer.class);
						setConversionService(localDateConverter());
					}
				}).build();
	}

	@Bean
	public ItemProcessor<Customer, Customer> itemProcessor() {
		return new ItemProcessor<Customer, Customer>() {
			@Override
			public Customer process(final Customer item) throws Exception {
				return item.increaseAmountBy(FIXED_AMOUNT);
			}
		};
	}

	@StepScope // Mandatory for using jobParameters
	@Bean
	public FlatFileItemWriter<Customer> itemWriter(
			@Value("#{stepExecutionContext['outputFile']}") final String outputFile) {
		return new FlatFileItemWriterBuilder<Customer>() //
				.name("itemWriter") //
				.resource(new FileSystemResource(outputFile)) //
				.delimited() //
				.delimiter(";") //
				.names("number", "firstName", "lastName", "address", "city", "postCode", "state", "birthDate") //
				.build();
	}

}
