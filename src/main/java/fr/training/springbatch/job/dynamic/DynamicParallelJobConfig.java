package fr.training.springbatch.job.dynamic;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import fr.training.springbatch.app.dto.Customer;
import fr.training.springbatch.app.job.AbstractJobConfiguration;

public class DynamicParallelJobConfig extends AbstractJobConfiguration {

	@Value("${application.dynamic-steps.chunksize:10}")
	private int chunkSize;

	List<String> filenames = Arrays.asList("customer01.csv", "customer02.csv");

	@Bean
	public Job dynamicParallelJob() {

		final List<Step> steps = filenames.stream().map(this::createStep).collect(Collectors.toList());

		return jobBuilderFactory.get("dynamicParallelJob") //
				.start(createParallelFlow(steps)) //
				.end() //
				.listener(reportListener()) //
				.build();
	}

	// helper method to create a split flow out of a List of steps
	private static Flow createParallelFlow(final List<Step> steps) {
		final SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
		taskExecutor.setConcurrencyLimit(steps.size());

		final List<Flow> flows = steps.stream() // we have to convert the steps to a flows
				.map(step -> //
				new FlowBuilder<Flow>("flow_" + step.getName()) //
				.start(step) //
				.build()) //
				.collect(Collectors.toList());

		return new FlowBuilder<SimpleFlow>("parallelStepsFlow") //
				.split(taskExecutor) //
				.add(flows.toArray(new Flow[flows.size()])) //
				.build();
	}

	// helper method to create a step
	private Step createStep(final String filename) {

		return stepBuilderFactory.get("step-for-" + filename) // !!! Stepname has to be unique
				.<Customer, Customer>chunk(chunkSize)
				.reader(fileReader(new FileSystemResource(new File("src/main/resources/csv/big/" + filename))))
				.writer(fileWriter(new FileSystemResource("target/output/big/" + filename))).build();
	}

	private ItemReader<Customer> fileReader(final FileSystemResource fileSystemResource) {
		return new FlatFileItemReaderBuilder<Customer>() //
				.name("itemReader") //
				.resource(fileSystemResource) //
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

	private ItemWriter<Customer> fileWriter(final FileSystemResource fileSystemResource) {
		return new FlatFileItemWriterBuilder<Customer>() //
				.name("itemWriter") //
				.resource(fileSystemResource) //
				.delimited() //
				.delimiter(";") //
				.names("number", "firstName", "lastName", "address", "city", "postCode", "state", "birthDate") //
				.build();
	}



}
