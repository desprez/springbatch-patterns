package fr.training.springbatch.job.controlbreak;

import static fr.training.springbatch.tools.validator.ParameterRequirement.fileExist;
import static fr.training.springbatch.tools.validator.ParameterRequirement.fileWritable;
import static fr.training.springbatch.tools.validator.ParameterRequirement.required;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.RecordFieldSetMapper;
import org.springframework.batch.item.support.SingleItemPeekableItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import fr.training.springbatch.app.dto.Transaction;
import fr.training.springbatch.app.job.AbstractJobConfiguration;
import fr.training.springbatch.tools.validator.AdditiveJobParametersValidatorBuilder;
import fr.training.springbatch.tools.validator.JobParameterRequirementValidator;
import fr.training.springbatch.tools.writer.ConsoleItemWriter;

/**
 * Another way to return Transactions list from the reader (similar to groupingRecordJob) but use the ItemListPeekableItemReader that use a Strategy pattern
 * (see BreakKeyStrategy.java) to groups records that have same "group" key (ie the customer number) in a easy configurable way (to sum records in this
 * example).
 *
 * @author desprez
 */
@Configuration
@ConditionalOnProperty(name = "spring.batch.job.names", havingValue = ControlBreakChunkJobConfig.CONTROLBREAK_CHUNK_JOB)
public class ControlBreakChunkJobConfig extends AbstractJobConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(ControlBreakChunkJobConfig.class);

    protected static final String CONTROLBREAK_CHUNK_JOB = "controlbreak-chunk-job";

    @Value("${application.controlbreak-step.chunksize:10}")
    private int chunkSize;

    @Bean
    Job controlBreakChunkJob(final Step controlBreakStep, final JobRepository jobRepository) {
        return new JobBuilder(CONTROLBREAK_CHUNK_JOB, jobRepository)
                .incrementer(new RunIdIncrementer()) // job can be launched as many times as desired
                .validator(new AdditiveJobParametersValidatorBuilder()
                        .addValidator(new JobParameterRequirementValidator("transaction-file", required().and(fileExist())))
                        .addValidator(new JobParameterRequirementValidator("output-file", required().and(fileWritable())))
                        .build())
                .start(controlBreakStep)
                .listener(reportListener())
                .build();
    }

    /**
     * @param controlBreakReader
     *            the injected Transaction {@link SingleItemPeekableItemReader} bean.
     * @param transactionWriter
     *            the injected Transaction {@link ItemWriter}.
     * @param completionPolicy
     * @return a Step Bean
     */
    @Bean
    Step controlBreakChunkStep(final JobRepository jobRepository, final PlatformTransactionManager transactionManager,
            final ItemPeekingCompletionPolicyReader<Transaction> breakKeyCompletionPolicy, final ItemWriter<Transaction> transactionWriter) {

        return new StepBuilder("controlbreak-step", jobRepository)
                .<Transaction, Transaction> chunk(breakKeyCompletionPolicy, transactionManager)
                .reader(breakKeyCompletionPolicy)
                .writer(transactionWriter)
                .listener(reportListener())
                .listener(chunklistener())
                .build();
    }

    private ChunkListener chunklistener() {
        // TODO Auto-generated method stub
        return new ChunkListener() {

            @Override
            public void beforeChunk(final ChunkContext context) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterChunkError(final ChunkContext context) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterChunk(final ChunkContext context) {
                logger.info("after chunk");
            }
        };
    }

    @Bean
    ItemPeekingCompletionPolicyReader<Transaction> breakKeyCompletionPolicy(final SingleItemPeekableItemReader<Transaction> controlBreakReader) {
        final ItemPeekingCompletionPolicyReader<Transaction> policy = new ItemPeekingCompletionPolicyReader<>();
        policy.setDelegate(controlBreakReader);
        policy.setBreakKeyStrategy((item1, item2) -> !item1.customerNumber().equals(item1.customerNumber()));
        return policy;
    }

    @Bean(destroyMethod = "")
    SingleItemPeekableItemReader<Transaction> controlBreakReader(final FlatFileItemReader<Transaction> transactionReader) {

        final SingleItemPeekableItemReader<Transaction> groupReader = new SingleItemPeekableItemReader<>();
        groupReader.setDelegate(transactionReader);

        return groupReader;
    }

    @StepScope // Mandatory for using jobParameters
    @Bean
    FlatFileItemReader<Transaction> transactionReader(@Value("#{jobParameters['transaction-file']}") final String transactionFile /* injected by Spring */) {

        return new FlatFileItemReaderBuilder<Transaction>()
                .name("transactionReader")
                .resource(new FileSystemResource(transactionFile))
                .delimited()
                .delimiter(";")
                .names("customerNumber", "number", "transactionDate", "amount")
                .linesToSkip(1)
                .fieldSetMapper(new RecordFieldSetMapper<Transaction>(Transaction.class, localDateConverter()))
                .build();
    }

    /**
     * @param outputFile
     *            the injected output file job parameter
     * @return a {@link FlatFileItemWriter} bean
     */
    // @StepScope // Mandatory for using jobParameters
    // @Bean
    // public FlatFileItemWriter<Transaction> transactionWriter(
    // @Value("#{jobParameters['output-file']}") final String outputFile) {
    //
    // return new FlatFileItemWriterBuilder<Transaction>().name("transactionWriter")
    // .resource(new FileSystemResource(outputFile)) //
    // .delimited() //
    // .delimiter(";") //
    // .names("customerNumber", "number", "transactionDate", "amount") //
    // .build();
    //
    // }

    @Bean
    ConsoleItemWriter<Transaction> transactionWriter() {
        return new ConsoleItemWriter<>();
    }

}
