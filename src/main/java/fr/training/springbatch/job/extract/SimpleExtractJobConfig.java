package fr.training.springbatch.job.extract;

import static fr.training.springbatch.tools.validator.ParameterRequirement.directoryExist;
import static fr.training.springbatch.tools.validator.ParameterRequirement.required;

import java.io.File;

import javax.sql.DataSource;

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
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.WritableResource;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.StringUtils;

import fr.training.springbatch.app.dto.Transaction;
import fr.training.springbatch.app.job.AbstractJobConfiguration;
import fr.training.springbatch.tools.validator.JobParameterRequirementValidator;

/**
 * <b>Pattern #1</b> This is the simplest job configuration (no really innovation here). One step use the reader / processor / writer pattern to read a database
 * table and write the content "as is" to a comma separated flat file. <br>
 * <b>Specificity</b> : the incrementalFilename method get an unique filename resource according to a file name and a job unique run identifier (Must be used in
 * conjunction with RunIdIncrementer).
 *
 * @author desprez
 */
@Configuration
@ConditionalOnProperty(name = "spring.batch.job.names", havingValue = SimpleExtractJobConfig.SIMPLE_EXTRACT_JOB)
public class SimpleExtractJobConfig extends AbstractJobConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(SimpleExtractJobConfig.class);

    protected static final String SIMPLE_EXTRACT_JOB = "simple-extract-job";

    private static final String FILENAME = "simple-extract.csv";

    @Value("${application.simple-extract-step.chunksize:10}")
    private int chunkSize;

    @Autowired
    public DataSource dataSource;

    @Bean
    Job simpleExtractJob(final Step extractStep, final JobRepository jobRepository) {
        return new JobBuilder(SIMPLE_EXTRACT_JOB, jobRepository)
                .validator(new JobParameterRequirementValidator("output-dir", required().and(directoryExist())))
                .incrementer(new RunIdIncrementer())
                .flow(extractStep)
                .end()
                .listener(reportListener())
                .build();
    }

    @Bean
    Step extractStep(final JobRepository jobRepository, final PlatformTransactionManager transactionManager,
            final FlatFileItemWriter<Transaction> extractWriter) {

        return new StepBuilder("simple-extract-step", jobRepository)
                .<Transaction, Transaction> chunk(chunkSize, transactionManager)
                .reader(simpleExtractReader())
                .processor(simpleExtractProcessor())
                .writer(extractWriter)
                .listener(progressListener())
                .build();
    }

    /**
     * ItemReader is an abstract representation of how data is provided as input to a Step. When the inputs are exhausted, the ItemReader returns null.
     */
    @Bean
    JdbcCursorItemReader<Transaction> simpleExtractReader() {

        return new JdbcCursorItemReaderBuilder<Transaction>()
                .name("simpleExtractReader")
                .dataSource(dataSource)
                .sql("SELECT customer_number, number, amount, transaction_date FROM Transaction ORDER BY customer_number, number ASC") //
                .rowMapper(new DataClassRowMapper<>(Transaction.class)) //
                .build();
    }

    /**
     * ItemProcessor represents the business processing of an item. The data read by ItemReader can be passed on to ItemProcessor. In this unit, the data is
     * transformed and sent for writing. If, while processing the item, it becomes invalid for further processing, you can return null. The nulls are not
     * written by ItemWriter.
     */
    @Bean
    ItemProcessor<Transaction, Transaction> simpleExtractProcessor() {
        return transaction -> {
            logger.debug("Processing {}", transaction);
            return transaction;
        };
    }

    /**
     * ItemWriter is the output of a Step.
     *
     * @param incrementalFilename
     *            spring injected resource
     */
    @Bean
    FlatFileItemWriter<Transaction> simpleExtractWriter(final WritableResource incrementalFilename) {

        return new FlatFileItemWriterBuilder<Transaction>()
                .name("simpleExtractWriter")
                .resource(incrementalFilename)
                .delimited()
                .delimiter(";")
                .names("customerNumber", "number", "transactionDate", "amount")
                .headerCallback(writer -> writer.write("customerNumber;number;transactionDate;amount"))
                .build();
    }

    /**
     * Get an unique filename resource according to a file name and a job unique run identifier.
     *
     * <b>Must be used in conjunction with RunIdIncrementer</b>
     *
     * @param outputdir
     *            the output directory job parameter
     * @param runId
     *            the job run id
     * @return a Resource Bean according to de
     */
    @StepScope // Mandatory for using jobParameters
    @Bean
    WritableResource incrementalFilename(@Value("#{jobParameters['output-dir']}") final String outputdir,
            @Value("#{jobParameters['run.id']}") final Long runId) {

        final String baseFilename = StringUtils.stripFilenameExtension(FILENAME);
        final String extension = StringUtils.getFilenameExtension(FILENAME);

        final File dir = new File(outputdir);
        final String name = String.format("%s-%s.%s", baseFilename, runId, extension);
        final File file = new File(dir, name);

        logger.info("fileName={}", file.getAbsoluteFile());
        return new FileSystemResource(file);
    }
}
