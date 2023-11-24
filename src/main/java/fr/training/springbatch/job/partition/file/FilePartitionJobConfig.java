package fr.training.springbatch.job.partition.file;

import static fr.training.springbatch.tools.validator.ParameterRequirement.directoryExist;
import static fr.training.springbatch.tools.validator.ParameterRequirement.required;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.support.MultiResourcePartitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import fr.training.springbatch.app.dto.Customer;
import fr.training.springbatch.app.job.AbstractJobConfiguration;
import fr.training.springbatch.tools.listener.OutputFileListener;
import fr.training.springbatch.tools.validator.AdditiveJobParametersValidatorBuilder;
import fr.training.springbatch.tools.validator.JobParameterRequirementValidator;

/**
 * <b>Pattern #14</b>
 *
 * @author Desprez
 */
@Configuration
@ConditionalOnProperty(name = "spring.batch.job.names", havingValue = FilePartitionJobConfig.PARTITION_JOB)
public class FilePartitionJobConfig extends AbstractJobConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(FilePartitionJobConfig.class);

    protected static final String PARTITION_JOB = "partition-job";

    public static final BigDecimal FIXED_AMOUNT = new BigDecimal("5");

    @Bean
    Job partitionJob(final Step masterStep, final JobRepository jobRepository) {

        return new JobBuilder(PARTITION_JOB, jobRepository)
                .validator(new AdditiveJobParametersValidatorBuilder()
                        .addValidator(new JobParameterRequirementValidator("input-path", required()))
                        .addValidator(new JobParameterRequirementValidator("output-path", required().and(directoryExist())))
                        .build())
                .start(masterStep)//
                .build();
    }

    // Master
    @Bean
    Step masterStep(final JobRepository jobRepository, final Step slaveStep, final MultiResourcePartitioner partitioner) throws IOException {

        return new StepBuilder("master-step", jobRepository)
                .partitioner(slaveStep.getName(), partitioner)
                .step(slaveStep)
                .gridSize(4)
                .taskExecutor(new SimpleAsyncTaskExecutor())
                .build();
    }

    @StepScope // Mandatory for using jobParameters
    @Bean
    MultiResourcePartitioner partitioner(@Value("#{jobParameters['input-path']}") final Resource[] inputResources) throws IOException {
        final MultiResourcePartitioner partitioner = new MultiResourcePartitioner();
        partitioner.partition(10);
        partitioner.setResources(inputResources);
        return partitioner;
    }

    // slave step
    @Bean
    Step slaveStep(final JobRepository jobRepository,
            final PlatformTransactionManager transactionManager,
            final FlatFileItemReader<Customer> itemReader,
            final ItemProcessor<Customer, Customer> processor,
            final FlatFileItemWriter<Customer> itemWriter,
            final OutputFileListener fileNameListener) {

        return new StepBuilder("slave-step", jobRepository)
                .<Customer, Customer> chunk(10, transactionManager)
                .reader(itemReader)
                .processor(processor)
                .writer(itemWriter)
                .listener(fileNameListener)
                .build();
    }

    @StepScope // Mandatory for using jobParameters
    @Bean
    OutputFileListener fileNameListener(@Value("#{jobParameters['output-path']}") final String outputPath) {
        final OutputFileListener listener = new OutputFileListener();
        listener.setPath(outputPath);
        return listener;
    }

    @StepScope // Mandatory for using jobParameters
    @Bean
    FlatFileItemReader<Customer> itemReader(@Value("#{stepExecutionContext['fileName']}") final String fileName) throws MalformedURLException {
        logger.info("fileName {}", fileName);

        return new FlatFileItemReaderBuilder<Customer>()
                .name("itemReader")
                .resource(new UrlResource(fileName))
                .delimited()
                .delimiter(";")
                .names("number", "firstName", "lastName", "address", "city", "postCode", "state", "birthDate")
                // .linesToSkip(1)
                .fieldSetMapper(new BeanWrapperFieldSetMapper<Customer>() {
                    {
                        setTargetType(Customer.class);
                        setConversionService(localDateConverter());
                    }
                }).build();
    }

    @Bean
    ItemProcessor<Customer, Customer> itemProcessor() {
        return item -> item.increaseAmountBy(FIXED_AMOUNT);
    }

    @StepScope // Mandatory for using jobParameters
    @Bean
    FlatFileItemWriter<Customer> itemWriter(@Value("#{stepExecutionContext['outputFile']}") final String outputFile) {
        return new FlatFileItemWriterBuilder<Customer>()
                .name("itemWriter")
                .resource(new FileSystemResource(outputFile))
                .delimited()
                .delimiter(";")
                .names("number", "firstName", "lastName", "address", "city", "postCode", "state", "birthDate")
                .build();
    }

}