package fr.training.springbatch.job.extract.processindicator;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;

import fr.training.springbatch.app.dto.Transaction;
import fr.training.springbatch.app.job.AbstractJobConfiguration;
import fr.training.springbatch.tools.tasklet.JdbcTasklet;

/**
 *
 * @author desprez
 */
public class ExtractProcessIndicatorJobConfig extends AbstractJobConfiguration {

    private static final String DONE = "Y";

    @Value("${application.createCsvFile-step.chunksize:10}")
    private int chunkSize;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    @Bean
    Job extractProcessIndicatorJob(final Step extractProcessIndicatorStep, final Step processedRemoverStep) {
        return jobBuilderFactory.get("extract-process-indicator-job") //
                .incrementer(new RunIdIncrementer()) //
                .start(extractProcessIndicatorStep) //
                .next(processedRemoverStep) //
                .listener(reportListener()) //
                .build();
    }

    @Bean
    Step extractProcessIndicatorStep(final ItemReader<Transaction> unprocessedReader, final FlatFileItemWriter<Transaction> csvFileWriter) {

        return stepBuilderFactory.get("extract-process-indicator-step") //
                .<Transaction, Transaction> chunk(chunkSize) //
                .reader(unprocessedReader) //
                .processor(processedMarker()) //
                .writer(csvFileWriter) //
                .listener(reportListener()) //
                .build();
    }

    @Bean
    Step processedRemoverStep() {

        return stepBuilderFactory.get("processedRemover-step") //
                .tasklet(processedItemsRemover()) //
                .build();

    }

    /**
     * The ItemReader unprocessedBillsReader always reads 1000 ids of unprocessed records and returns them one after another.
     */
    @Bean
    @DependsOnDatabaseInitialization
    JdbcPagingItemReader<Transaction> unprocessedReader(final DataSource dataSource, final PagingQueryProvider queryProvider) {

        return new JdbcPagingItemReaderBuilder<Transaction>() //
                .name("unprocessedReader") //
                .dataSource(dataSource) //
                .pageSize(chunkSize) //
                .queryProvider(queryProvider)//
                .rowMapper(new BeanPropertyRowMapper<>(Transaction.class)) //
                .build();

    }

    @Bean
    @DependsOnDatabaseInitialization
    SqlPagingQueryProviderFactoryBean queryProvider(final DataSource dataSource) {
        final SqlPagingQueryProviderFactoryBean provider = new SqlPagingQueryProviderFactoryBean();

        provider.setDataSource(dataSource);
        provider.setSelectClause("SELECT customer_number, number, amount, transaction_date");
        provider.setFromClause("FROM Transaction");
        provider.setWhereClause("WHERE processed <> '" + DONE + "'");
        provider.setSortKeys(sortByCustomerNumberAsc());

        return provider;
    }

    private Map<String, Order> sortByCustomerNumberAsc() {
        final Map<String, Order> sortConfiguration = new LinkedHashMap<>();
        sortConfiguration.put("customer_number", Order.ASCENDING);
        sortConfiguration.put("number", Order.ASCENDING);
        sortConfiguration.put("transaction_date", Order.ASCENDING);
        return sortConfiguration;
    }

    /**
     * The ItemProcessor processedMarker reads the corresponding items from the database and marks them as processed.
     */
    @Bean
    ItemProcessor<Transaction, Transaction> processedMarker() {
        return item -> {
            markAsProcessed(item);
            return item;
        };
    }

    /**
     *
     * @param item
     */
    private void markAsProcessed(final Transaction item) {
        jdbcTemplate.update("UPDATE Transaction SET processed=? WHERE customer_number=? AND number=?", (PreparedStatementSetter) ps -> {
            ps.setString(1, DONE);
            ps.setLong(2, item.getCustomerNumber());
            ps.setString(3, item.getNumber());
        });
    }

    /**
     * The ItemWriter csvFileWriter writes them to a CSV file. The path of this file is provided as batch parameter ("outputFile").
     */
    @StepScope // Mandatory for using jobParameters
    @Bean
    FlatFileItemWriter<Transaction> csvFileWriter(@Value("#{jobParameters['output-file']}") final String outputFile) {

        return new FlatFileItemWriterBuilder<Transaction>() //
                .name("csvFileWriter") //
                .resource(new FileSystemResource(outputFile)) //
                .delimited() //
                .delimiter(";") //
                .names("customerNumber", "number", "transactionDate", "amount") //
                .headerCallback(writer -> writer.write("customerNumber;number;transactionDate;amount")) //
                .build();
    }

    /**
     * The tasklet processedItemsRemover deletes all record marked as processed.
     */
    @Bean
    Tasklet processedItemsRemover() {
        final JdbcTasklet deleteRecordTasklet = new JdbcTasklet();
        deleteRecordTasklet.setDataSource(dataSource);
        deleteRecordTasklet.setSql("DELETE FROM Transaction WHERE processed = '" + DONE + "'");
        return deleteRecordTasklet;
    }

}
