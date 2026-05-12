package fr.training.springbatch.job.computedelta;

import fr.training.springbatch.app.dto.Stock;
import fr.training.springbatch.app.job.AbstractJobConfiguration;
import fr.training.springbatch.tools.tasklet.SqlExecutingTasklet;
import fr.training.springbatch.tools.validator.JobParameterRequirementValidator;
import fr.training.springbatch.tools.writer.ConsoleItemWriter;
import fr.training.springbatch.tools.writer.NoOpWriter;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.NoWorkFoundStepExecutionListener;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.database.JdbcPagingItemReader;
import org.springframework.batch.infrastructure.item.database.Order;
import org.springframework.batch.infrastructure.item.database.PagingQueryProvider;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.infrastructure.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.infrastructure.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.item.support.builder.CompositeItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Collections;

import static fr.training.springbatch.tools.validator.ParameterRequirement.*;

/**
 * <b>Pattern #16</b> Imagine that you receive a different file every day from your partner with all the data.
 *
 * And that you have to update your system with the added or deleted data, this is exactly what this job does, it compute the delta between the file received at
 * day D-1 with the file received at day D (usually used in companies that use files to transmit data).
 *
 * @author Desprez
 */
@Configuration
@ConditionalOnProperty(name = "spring.batch.job.names", havingValue = ComputeDeltaJobConfig.COMPUTE_DELTA_JOB)
public class ComputeDeltaJobConfig extends AbstractJobConfiguration {

    protected static final String COMPUTE_DELTA_JOB = "compute-delta-job";

    @Value("${application.compute-delta-load-step.chunksize:10}")
    private int chunkSize;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * For this job, jobinstance are defined by the <b>today-stock-file</b> JobParameter to ensure that the same file not be processed more than once.
     */
    @Bean
    Job computeDeltaJob(final Step createTable, final Step loadTodayStock, final Step processAddedItems, final Step processRemovedItems,
            final JobRepository jobRepository, final Step swapTables) {

        return new JobBuilder(COMPUTE_DELTA_JOB, jobRepository)
                .validator(new JobParameterRequirementValidator("today-stock-file", required().and(identifying()).and(fileExist())))
                .listener(reportListener())
                .start(createTable) // create D table
                .next(loadTodayStock) // load stock D file to D table
                .next(processAddedItems) // process items present in D table but not in D-1 table = added
                .next(processRemovedItems) // process items present in D-1 table but not in D table = removed
                .next(swapTables) // drop D-1 table and rename D table to D-1 table
                .build();
    }

    /**
     * Create the Stock of Day table
     */
    @Bean
    Step createTable(final JobRepository jobRepository, final PlatformTransactionManager transactionManager) {

        final String createSql = "CREATE TABLE today_stock(number BIGINT NOT NULL, label VARCHAR(50), PRIMARY KEY (number));";

        return new StepBuilder("swap-step", jobRepository)
                .tasklet(new SqlExecutingTasklet(jdbcTemplate, createSql), transactionManager) //
                .build();
    }

    /**
     * Load stock Stock of the Day File file to the Stock of the Day table.
     */
    @Bean
    Step loadTodayStock(final JobRepository jobRepository, final PlatformTransactionManager transactionManager, final FlatFileItemReader<Stock> fileReader,
            final JdbcBatchItemWriter<Stock> jdbcWriter) {

        return new StepBuilder("load-today-stock", jobRepository)
                .<Stock, Stock> chunk(chunkSize)
                .transactionManager(transactionManager)
                .reader(fileReader)
                .writer(jdbcWriter)
                .listener(new NoWorkFoundStepExecutionListener()) // Force job to fail if stock N file is empty
                .build();
    }

    /**
     * Read the Stock of the Day File.
     *
     * @param stockFile
     *            the stock of the Day file
     */
    @StepScope // Mandatory for using jobParameters
    @Bean
    FlatFileItemReader<Stock> fileReader(@Value("#{jobParameters['today-stock-file']}") final String stockFile) {
        return new FlatFileItemReaderBuilder<Stock>()
                .name("fileReader")
                .resource(new FileSystemResource(stockFile))
                .delimited()
                .delimiter(";")
                .names("number", "label")
                .targetType(Stock.class)
                .saveState(true)
                .build();
    }

    /**
     * Write lines of the stock of the Day File into the <b>today_stock</b> table.
     */
    @Bean
    @DependsOnDatabaseInitialization
    JdbcBatchItemWriter<Stock> jdbcWriter() {
        return new JdbcBatchItemWriterBuilder<Stock>()
                .dataSource(dataSource)
                .sql("INSERT INTO today_stock (number, label) VALUES (:number, :label)")
                .beanMapped()
                .build();
    }

    /**
     * Process item present in D table but not in D-1 table (= added).
     */
    @Bean
    Step processAddedItems(final JobRepository jobRepository, final PlatformTransactionManager transactionManager,
            final JdbcPagingItemReader<Stock> jdbcAddedItemReader) {
        return new StepBuilder("process-added-step", jobRepository)
                .<Stock, Stock> chunk(chunkSize)
                .transactionManager(transactionManager)
                .reader(jdbcAddedItemReader)
                .writer(new CompositeItemWriterBuilder<Stock>().delegates(addedWriter(), writer2()).build()) //
                .build();
    }

    /**
     * Process item present in D-1 table but not in D table (= removed).
     */
    @Bean
    Step processRemovedItems(final JobRepository jobRepository, final PlatformTransactionManager transactionManager,
            final JdbcPagingItemReader<Stock> jdbcRemovedItemReader) {
        return new StepBuilder("process-removed-step", jobRepository)
                .<Stock, Stock> chunk(chunkSize)
                .transactionManager(transactionManager)
                .reader(jdbcRemovedItemReader)
                .writer(new CompositeItemWriterBuilder<Stock>().delegates(removedWriter(), writer2()).build()) //
                .build();
    }

    @Bean
    @DependsOnDatabaseInitialization
    JdbcPagingItemReader<Stock> jdbcAddedItemReader(final PagingQueryProvider addedItemsQueryProvider) throws Exception {

        return new JdbcPagingItemReaderBuilder<Stock>()
                .name("jdbcAddedItemReader")
                .dataSource(dataSource)
                .pageSize(100)
                .queryProvider(addedItemsQueryProvider)
                .rowMapper(new DataClassRowMapper<>(Stock.class))
                .build();
    }

    @Bean
    @DependsOnDatabaseInitialization
    PagingQueryProvider addedItemsQueryProvider() throws Exception {
        final SqlPagingQueryProviderFactoryBean provider = new SqlPagingQueryProviderFactoryBean();

        provider.setDataSource(dataSource);
        provider.setSelectClause("SELECT number, label");
        provider.setFromClause("FROM today_stock");
        provider.setWhereClause("WHERE number NOT IN (SELECT number FROM yesterday_stock)");

        provider.setSortKeys(Collections.singletonMap("number", Order.ASCENDING));

        return provider.getObject();
    }

    @Bean
    @DependsOnDatabaseInitialization
    JdbcPagingItemReader<Stock> jdbcRemovedItemReader(final PagingQueryProvider removedItemsQueryProvider) throws Exception {

        return new JdbcPagingItemReaderBuilder<Stock>()
                .name("jdbcRemovedItemReader")
                .dataSource(dataSource)
                .pageSize(100)
                .queryProvider(removedItemsQueryProvider)
                .rowMapper(new DataClassRowMapper<>(Stock.class))
                .build();
    }

    @Bean
    @DependsOnDatabaseInitialization
    PagingQueryProvider removedItemsQueryProvider() throws Exception {
        final SqlPagingQueryProviderFactoryBean provider = new SqlPagingQueryProviderFactoryBean();

        provider.setDataSource(dataSource);
        provider.setSelectClause("SELECT number, label");
        provider.setFromClause("FROM yesterday_stock");
        provider.setWhereClause("WHERE number NOT IN (SELECT number FROM today_stock)");
        provider.setSortKeys(Collections.singletonMap("number", Order.ASCENDING));

        return provider.getObject();
    }

    /**
     * Fake writer to simulate writing added items.
     */
    private ItemWriter<Stock> addedWriter() {
        return new ConsoleItemWriter<>("added: ");
    }

    /**
     * Fake writer to simulate writing removed items.
     */
    private ItemWriter<Stock> removedWriter() {
        return new ConsoleItemWriter<>("removed: ");
    }

    /**
     * Other writer to show the {@link CompositeItemWriterBuilder} usage.
     */
    private ItemWriter<Stock> writer2() {
        return new NoOpWriter<>();
    }

    /**
     * Swap the stock of the Day table to the stock of the Day-1 table.
     */
    @Bean
    Step swapTables(final JobRepository jobRepository, final PlatformTransactionManager transactionManager) {

        final String dropSql = "DROP TABLE yesterday_stock;";
        final String renameSql = "ALTER TABLE today_stock RENAME TO yesterday_stock;";

        return new StepBuilder("swap-step", jobRepository) //
                .tasklet(new SqlExecutingTasklet(jdbcTemplate, dropSql, renameSql), transactionManager) //
                .build();
    }

}
