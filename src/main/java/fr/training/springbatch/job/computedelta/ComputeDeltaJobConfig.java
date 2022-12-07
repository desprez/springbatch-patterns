package fr.training.springbatch.job.computedelta;

import java.util.Collections;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.NoWorkFoundStepExecutionListener;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.support.builder.CompositeItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import fr.training.springbatch.app.dto.Stock;
import fr.training.springbatch.app.job.AbstractJobConfiguration;
import fr.training.springbatch.tools.tasklet.SqlExecutingTasklet;
import fr.training.springbatch.tools.writer.ConsoleItemWriter;
import fr.training.springbatch.tools.writer.NoOpWriter;

public class ComputeDeltaJobConfig extends AbstractJobConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(ComputeDeltaJobConfig.class);

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
    public Job computeDeltaJob(final Step createTable, final Step loadTodayStock, final Step processAddedItems, final Step processRemovedItems) {
        return jobBuilderFactory.get("compute-delta-job") //
                .incrementer(new RunIdIncrementer()) // job can be launched as many times as desired
                .validator(new DefaultJobParametersValidator(new String[] { "today-stock-file" }, new String[] {})) //
                .start(createTable) // create N table
                .next(loadTodayStock) // load stock N file to N table
                .next(processAddedItems) // process items present in N table but not in N-1 table = added
                .next(processRemovedItems) // process items present in N-1 table but not in N table = removed
                .next(swapTables()) // drop N-1 table and rename N table to N-1 table
                .listener(reportListener()) //
                .build();
    }

    /**
     * Create the stock N table
     */
    @Bean
    public Step createTable() {

        final String createSql = "CREATE TABLE today_stock(number BIGINT NOT NULL, label VARCHAR(50), PRIMARY KEY (number));";

        return stepBuilderFactory.get("swap-step") //
                .tasklet(new SqlExecutingTasklet(jdbcTemplate, createSql)) //
                .build();
    }

    /**
     * Load stock N file to N table.
     */
    @Bean
    public Step loadTodayStock(final FlatFileItemReader<Stock> fileReader, final JdbcBatchItemWriter<Stock> jdbcWriter) {

        return stepBuilderFactory.get("load-today-stock") //
                .<Stock, Stock> chunk(chunkSize) //
                .reader(fileReader) //
                .writer(jdbcWriter) //
                .listener(new NoWorkFoundStepExecutionListener()) // Force job to fail if stock N file is empty
                .build();
    }

    /**
     * Reade the stockFile.
     *
     * @param stockFile
     *            the stock N file
     */
    @StepScope // Mandatory for using jobParameters
    @Bean
    public FlatFileItemReader<Stock> fileReader(@Value("#{jobParameters['today-stock-file']}") final String stockFile) {
        return new FlatFileItemReaderBuilder<Stock>() //
                .name("fileReader") //
                .resource(new FileSystemResource(stockFile)) //
                .delimited() //
                .delimiter(";") //
                .names("number", "label") //
                .targetType(Stock.class) //
                .saveState(true) //
                .build();
    }

    /**
     * Write lines of the stockFile into the <b>today_stock</b> table.
     */
    @Bean
    public JdbcBatchItemWriter<Stock> jdbcWriter() {
        return new JdbcBatchItemWriterBuilder<Stock>() //
                .dataSource(dataSource).sql("INSERT INTO today_stock (number, label) VALUES (:number, :label)") //
                .beanMapped() //
                .build();
    }

    /**
     * Process item present in N table but not in N-1 table (= added).
     */
    @Bean
    public Step processAddedItems(final JdbcPagingItemReader<Stock> jdbcAddedItemReader) {
        return stepBuilderFactory.get("process-added-step") //
                .<Stock, Stock> chunk(chunkSize) //
                .reader(jdbcAddedItemReader) //
                .writer(new CompositeItemWriterBuilder<Stock>().delegates(addedWriter(), writer2()).build()) //
                .build();
    }

    /**
     * Process item present in N-1 table but not in N table (= removed).
     */
    @Bean
    public Step processRemovedItems(final JdbcPagingItemReader<Stock> jdbcRemovedItemReader) {
        return stepBuilderFactory.get("process-removed-step") //
                .<Stock, Stock> chunk(chunkSize) //
                .reader(jdbcRemovedItemReader) //
                .writer(new CompositeItemWriterBuilder<Stock>().delegates(removedWriter(), writer2()).build()) //
                .build();
    }

    @Bean
    public JdbcPagingItemReader<Stock> jdbcAddedItemReader(final PagingQueryProvider addedItemsQueryProvider) {

        return new JdbcPagingItemReaderBuilder<Stock>() //
                .name("jdbcAddedItemReader") //
                .dataSource(dataSource) //
                .pageSize(100) //
                .queryProvider(addedItemsQueryProvider)//
                .rowMapper(new BeanPropertyRowMapper<>(Stock.class)) //
                .build();
    }

    @Bean
    public PagingQueryProvider addedItemsQueryProvider() throws Exception {
        final SqlPagingQueryProviderFactoryBean provider = new SqlPagingQueryProviderFactoryBean();

        provider.setDataSource(dataSource);
        provider.setSelectClause("SELECT number, label");
        provider.setFromClause("FROM today_stock");
        provider.setWhereClause("WHERE number NOT IN (SELECT number FROM yesterday_stock)");

        provider.setSortKeys(Collections.singletonMap("number", Order.ASCENDING));

        return provider.getObject();
    }

    @Bean
    public JdbcPagingItemReader<Stock> jdbcRemovedItemReader(final PagingQueryProvider removedItemsQueryProvider) {

        return new JdbcPagingItemReaderBuilder<Stock>() //
                .name("jdbcRemovedItemReader") //
                .dataSource(dataSource) //
                .pageSize(100) //
                .queryProvider(removedItemsQueryProvider)//
                .rowMapper(new BeanPropertyRowMapper<>(Stock.class)) //
                .build();
    }

    @Bean
    public PagingQueryProvider removedItemsQueryProvider() throws Exception {
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
        return new ConsoleItemWriter<Stock>("added: ");
    }

    /**
     * Fake writer to simulate writing removed items.
     */
    private ItemWriter<Stock> removedWriter() {
        return new ConsoleItemWriter<Stock>("removed: ");
    }

    /**
     * Other writer to show the {@link CompositeItemWriterBuilder} usage.
     */
    private ItemWriter<Stock> writer2() {
        return new NoOpWriter<Stock>();
    }

    /**
     * Swap the N table to the N-1 table.
     */
    @Bean
    public Step swapTables() {

        final String dropSql = "DROP TABLE yesterday_stock;";
        final String renameSql = "ALTER TABLE today_stock RENAME TO yesterday_stock;";

        return stepBuilderFactory.get("swap-step") //
                .tasklet(new SqlExecutingTasklet(jdbcTemplate, dropSql, renameSql)) //
                .build();
    }

}
