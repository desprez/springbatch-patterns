package fr.training.springbatch.job.controlbreak;

import static fr.training.springbatch.tools.validator.ParameterRequirement.fileExist;
import static fr.training.springbatch.tools.validator.ParameterRequirement.fileWritable;
import static fr.training.springbatch.tools.validator.ParameterRequirement.required;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.RecordFieldSetMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import fr.training.springbatch.app.dto.Customer;
import fr.training.springbatch.app.dto.Transaction;
import fr.training.springbatch.app.dto.TransactionSum;
import fr.training.springbatch.app.job.AbstractJobConfiguration;
import fr.training.springbatch.tools.validator.AdditiveJobParametersValidatorBuilder;
import fr.training.springbatch.tools.validator.JobParameterRequirementValidator;

/**
 * <b>Pattern #8</b> This job groups all transactions by customer number and exports result to csv file.
 *
 * It use an {@link ItemListPeekableItemReader} used to get a list of {@link Transaction} grouped by {@link Customer}
 *
 * @author Desprez
 */
@Configuration
@ConditionalOnProperty(name = "spring.batch.job.names", havingValue = ControlBreakJobConfig.CONTROLBREAK_JOB)
public class ControlBreakJobConfig extends AbstractJobConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(ControlBreakJobConfig.class);

    protected static final String CONTROLBREAK_JOB = "controlbreak-job";

    @Value("${application.controlbreak-step.chunksize:10}")
    private int chunkSize;

    @Bean
    Job controlBreakJob(final Step controlBreakStep, final JobRepository jobRepository) {
        return new JobBuilder(CONTROLBREAK_JOB, jobRepository)
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
     *            the injected Transaction {@link ItemListPeekableItemReader} bean.
     * @param transactionSumWriter
     *            the injected TransactionSum {@link ItemWriter}.
     * @return a Step Bean
     */
    @Bean
    Step controlBreakStep(final JobRepository jobRepository, final PlatformTransactionManager transactionManager,
            final ItemListPeekableItemReader<Transaction> controlBreakReader, final ItemWriter<TransactionSum> transactionSumWriter /* injected by Spring */) {

        return new StepBuilder("controlbreak-step", jobRepository)
                .<List<Transaction>, TransactionSum> chunk(chunkSize, transactionManager)
                .reader(controlBreakReader)
                .processor(processor())
                .writer(transactionSumWriter)
                .listener(reportListener())
                .build();
    }

    @Bean(destroyMethod = "")
    ItemListPeekableItemReader<Transaction> controlBreakReader(final FlatFileItemReader<Transaction> transactionReader) {

        final ItemListPeekableItemReader<Transaction> groupReader = new ItemListPeekableItemReader<>();
        groupReader.setDelegate(transactionReader);
        groupReader.setBreakKeyStrategy((item1, item2) -> !item1.customerNumber().equals(item2.customerNumber()));
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
     * Processor that sum customer's transactions to compute his balance, fill and return new TransactionSum objects.
     *
     * @return the processor
     */
    private ItemProcessor<List<Transaction>, TransactionSum> processor() {
        return items -> {
            final double sum = items.stream().mapToDouble(Transaction::amount).sum();

            final TransactionSum transactionSum = new TransactionSum(items.get(0).customerNumber(),
                    new BigDecimal(sum).setScale(2, RoundingMode.HALF_UP).doubleValue());

            logger.debug(transactionSum.toString());
            return transactionSum;
        };
    }

    /**
     * @param outputFile
     *            the injected output file job parameter
     * @return a {@link FlatFileItemWriter} bean
     */
    @StepScope // Mandatory for using jobParameters
    @Bean
    FlatFileItemWriter<TransactionSum> transactionSumWriter(@Value("#{jobParameters['output-file']}") final String outputFile) {

        return new FlatFileItemWriterBuilder<TransactionSum>().name("transactionSumWriter").resource(new FileSystemResource(outputFile)) //
                .delimited()
                .delimiter(";")
                .names("customerNumber", "balance")
                .build();
    }

}