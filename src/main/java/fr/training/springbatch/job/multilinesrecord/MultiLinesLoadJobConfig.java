package fr.training.springbatch.job.multilinesrecord;

import static fr.training.springbatch.tools.validator.ParameterRequirement.fileExist;
import static fr.training.springbatch.tools.validator.ParameterRequirement.required;

import java.util.HashMap;
import java.util.Map;

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
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.mapping.PatternMatchingCompositeLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.batch.item.support.ClassifierCompositeItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.classify.Classifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import fr.training.springbatch.app.job.AbstractJobConfiguration;
import fr.training.springbatch.job.multilinesrecord.dto.CustomerRecord;
import fr.training.springbatch.job.multilinesrecord.dto.Record;
import fr.training.springbatch.job.multilinesrecord.dto.TransactionRecord;
import fr.training.springbatch.tools.validator.JobParameterRequirementValidator;

/**
 * <b>Pattern #19</b> This job allows to load records of different types from the same file and load them into their respective tables.
 *
 * It use a {@link PatternMatchingCompositeLineMapper} to map each line with a record Type and a {@link ClassifierCompositeItemWriter} to choose what table to
 * insert with.
 *
 * @author Desprez
 */
@Configuration
@ConditionalOnProperty(name = "spring.batch.job.names", havingValue = MultiLinesLoadJobConfig.MULTILINES_LOAD_JOB)
public class MultiLinesLoadJobConfig extends AbstractJobConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(MultiLinesLoadJobConfig.class);

    protected static final String MULTILINES_LOAD_JOB = "multilines-load-job";

    private static final String DELIMITER = ",";

    @Value("${application.multilines-load-step.chunksize:10}")
    private int chunkSize;

    @Autowired
    public DataSource dataSource;

    @Bean
    Job multilinesLoadJob(final Step multilinesLoadStep, final JobRepository jobRepository) {
        return new JobBuilder(MULTILINES_LOAD_JOB, jobRepository)
                .validator(new JobParameterRequirementValidator("input-file", required().and(fileExist())))
                .incrementer(new RunIdIncrementer())
                .flow(multilinesLoadStep)
                .end()
                .listener(reportListener())
                .build();
    }

    @Bean
    Step multilinesLoadStep(final JobRepository jobRepository, final PlatformTransactionManager transactionManager,
            final ClassifierCompositeItemWriter<Record> classifierRecordCompositeItemWriter, final ItemReader<Record> realFileReader) {

        return new StepBuilder("multilines-load-step", jobRepository)
                .<Record, Record> chunk(chunkSize, transactionManager)
                .reader(realFileReader)
                .processor(multilinesExtractProcessor())
                .writer(classifierRecordCompositeItemWriter)
                .listener(progressListener())
                .build();
    }

    private ItemProcessor<Record, Record> multilinesExtractProcessor() {
        return item -> {
            logger.debug("=> {}", item);
            return item;
        };
    }

    @StepScope
    @Bean
    FlatFileItemReader<Record> realFileReader(@Value("#{jobParameters['input-file']}") final String inputFile) {
        final FlatFileItemReader<Record> fileReader = new FlatFileItemReader<>();
        fileReader.setResource(new FileSystemResource(inputFile));
        fileReader.setLineMapper(lineMapper());
        fileReader.setEncoding("UTF-8");
        return fileReader;
    }

    public PatternMatchingCompositeLineMapper<Record> lineMapper() {
        final PatternMatchingCompositeLineMapper<Record> mapper = new PatternMatchingCompositeLineMapper<>();

        final Map<String, LineTokenizer> tokenizers = new HashMap<>(2);
        tokenizers.put("*,C,*", customerLineTokenizer());
        tokenizers.put("*,T,*", transactionLineTokenizer());

        mapper.setTokenizers(tokenizers);

        final Map<String, FieldSetMapper<Record>> mappers = new HashMap<>(2);
        mappers.put("*,C,*", customerFieldSetMapper());
        mappers.put("*,T,*", transactionFieldSetMapper());

        mapper.setFieldSetMappers(mappers);

        return mapper;
    }

    public LineTokenizer customerLineTokenizer() {
        final DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer(DELIMITER);
        tokenizer.setNames("number", "recordType", "firstName", "lastName", "address", "city", "postCode", "state");
        return tokenizer;
    }

    public LineTokenizer transactionLineTokenizer() {
        final DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer(DELIMITER);
        tokenizer.setNames("customerNumber", "recordType", "number", "transactionDate", "amount");
        return tokenizer;
    }

    public FieldSetMapper<Record> customerFieldSetMapper() {
        final BeanWrapperFieldSetMapper<Record> mapper = new BeanWrapperFieldSetMapper<>();
        mapper.setTargetType(CustomerRecord.class);
        mapper.setConversionService(localDateConverter());
        return mapper;
    }

    public FieldSetMapper<Record> transactionFieldSetMapper() {
        final BeanWrapperFieldSetMapper<Record> mapper = new BeanWrapperFieldSetMapper<>();
        mapper.setTargetType(TransactionRecord.class);
        mapper.setConversionService(localDateConverter());
        return mapper;
    }

    @Bean
    ClassifierCompositeItemWriter<?> classifierRecordCompositeItemWriter(final Classifier<Record, ItemWriter> recordClassifier) throws Exception {
        final ClassifierCompositeItemWriter compositeItemWriter = new ClassifierCompositeItemWriter();
        compositeItemWriter.setClassifier(recordClassifier);
        return compositeItemWriter;
    }

    @Bean
    Classifier<Record, ItemWriter> recordClassifier(final ItemWriter<CustomerRecord> customerWriter, final ItemWriter<TransactionRecord> transactionWriter) {

        return record -> record instanceof CustomerRecord ? customerWriter : transactionWriter;
    }

    @Bean
    @DependsOnDatabaseInitialization
    JdbcBatchItemWriter<CustomerRecord> customerWriter() {
        return new JdbcBatchItemWriterBuilder<CustomerRecord>()
                .dataSource(dataSource)
                .sql("INSERT INTO Customer(number, first_name, last_name, address, city, state, post_code, birth_date) "
                        + "VALUES (:number, :firstName, :lastName, :address, :city, :state, :postCode, :birthDate)")
                .beanMapped() //
                .build();
    }

    @Bean
    ItemWriter<TransactionRecord> transactionWriter() {
        return new JdbcBatchItemWriterBuilder<TransactionRecord>()
                .dataSource(dataSource)
                .sql("INSERT INTO Transaction(customer_number, number, transaction_date, amount) "
                        + "VALUES (:customerNumber, :number, :transactionDate, :amount)")
                .beanMapped()
                .build();
    }
}
