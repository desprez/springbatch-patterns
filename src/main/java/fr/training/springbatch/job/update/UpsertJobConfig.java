package fr.training.springbatch.job.update;

import static fr.training.springbatch.tools.validator.ParameterRequirement.fileExist;
import static fr.training.springbatch.tools.validator.ParameterRequirement.required;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import fr.training.springbatch.app.dto.Customer;
import fr.training.springbatch.app.job.AbstractJobConfiguration;
import fr.training.springbatch.tools.validator.JobParameterRequirementValidator;

/**
 * <b>Pattern #23</b> This job illustrates usage of the SQL Upsert command in a batch. <br>
 * <b>Note:</b> this command is not present in all database engines. For example, it is present in PostgreSQL but not in H2 or the syntax differs, which is why
 * this batch does not have a test class associated .
 *
 * @author Desprez
 */
@Configuration
@ConditionalOnProperty(name = "spring.batch.job.names", havingValue = UpsertJobConfig.UPSERT_JOB)
public class UpsertJobConfig extends AbstractJobConfiguration {

    // This syntax works well with PostgreSQL but not with H2
    private static final String SQL_UPSERT_SYNTAX = "INSERT INTO Customer(number, first_name, last_name, address, city, state, post_code, birth_date) "
            + "VALUES (:number, :firstName, :lastName, :address, :city, :state, :postCode, :birthDate) "
            + "ON CONFLICT (number) DO UPDATE SET "
            + "    first_name = EXCLUDED.first_name, "
            + "    last_name = EXCLUDED.last_name, "
            + "    address = EXCLUDED.address, "
            + "    city = EXCLUDED.city, "
            + "    state = EXCLUDED.state, "
            + "    post_code = EXCLUDED.post_code, "
            + "    birth_date = EXCLUDED.birth_date";

    // This syntax work well with H2 but not with PostgreSQL
    private static final String SQL_MERGE_SYNTAX = "MERGE INTO Customer AS t "
            + "USING ( "
            + "    VALUES  (:number, :firstName, :lastName, :address, :city, :state, :postCode, :birthDate) "
            + "       ) AS s (number, first_name, last_name, address, city, state, post_code, birth_date) "
            + "    ON t.number = s.number "
            + "    WHEN MATCHED THEN "
            + "        UPDATE SET t.first_name=s.first_name, t.last_name=s.last_name, t.address=s.address, t.city=s.city, t.state=s.state, t.post_code=s.post_code, t.birth_date=s.birth_date "
            + "    WHEN NOT MATCHED THEN "
            + "        INSERT (number, first_name, last_name, address, city, state, post_code, birth_date) "
            + "              VALUES (s.number, s.first_name, s.last_name, s.address, s.city, s.state, s.post_code, s.birth_date)";

    protected static final String UPSERT_JOB = "upsert-job";

    @Value("${application.upsert-step.chunksize:10}")
    private int chunkSize;

    @Autowired
    private DataSource dataSource;

    @Bean
    Job upsertJob(final Step upsertStep, final JobRepository jobRepository) {
        return new JobBuilder(UPSERT_JOB, jobRepository)
                .incrementer(new RunIdIncrementer())
                .validator(new JobParameterRequirementValidator("input-file", required().and(fileExist())))
                .start(upsertStep)
                .build();
    }

    @Bean
    Step upsertStep(final JobRepository jobRepository, final PlatformTransactionManager transactionManager,
            final FlatFileItemReader<Customer> fileReader, final JdbcBatchItemWriter<Customer> upsertWriter) {

        return new StepBuilder("upsert-step", jobRepository)
                .<Customer, Customer> chunk(chunkSize, transactionManager)
                .reader(fileReader)
                .writer(upsertWriter)
                .build();
    }

    @StepScope // Mandatory for using jobParameters
    @Bean
    FlatFileItemReader<Customer> fileReader(@Value("#{jobParameters['input-file']}") final String inputFile) {
        return new FlatFileItemReaderBuilder<Customer>()
                .name("fileReader")
                .resource(new FileSystemResource(inputFile))
                .delimited()
                .delimiter(";")
                .names("number", "firstName", "lastName", "address", "city", "state", "postCode", "birtDate")
                .linesToSkip(1)
                .fieldSetMapper(new BeanWrapperFieldSetMapper<Customer>() {
                    {
                        setTargetType(Customer.class);
                        setConversionService(localDateConverter());
                    }
                }).build();
    }

    @Bean
    @DependsOnDatabaseInitialization
    JdbcBatchItemWriter<Customer> upsertWriter() {
        return new JdbcBatchItemWriterBuilder<Customer>()
                .dataSource(dataSource)
                // .sql(SQL_UPSERT_SYNTAX)
                .sql(SQL_MERGE_SYNTAX)
                .beanMapped()
                .build();
    }

}