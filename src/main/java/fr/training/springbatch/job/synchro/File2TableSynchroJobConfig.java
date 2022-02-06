package fr.training.springbatch.job.synchro;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import fr.training.springbatch.app.dto.Customer;
import fr.training.springbatch.app.dto.Transaction;
import fr.training.springbatch.job.synchro.component.MasterDetailReader;
import fr.training.springbatch.tools.synchro.CompositeAggregateReader;
import fr.training.springbatch.tools.synchro.ItemAccumulator;

/**
 * Using {@link ItemAccumulator} & {@link MasterDetailReader} to "synchronize" 1
 * File and 1 table who share the same "customer number" key.
 * <ul>
 * <li>one master file : customer csv file</li>
 * <li>one detail table : transaction</li>
 * </ul>
 *
 * Datas from the detail file (transaction) are stored in the item (customer)
 * returned by the "Master" reader.
 *
 * @author Desprez
 */
public class File2TableSynchroJobConfig extends AbstractSynchroJob {

	private static final Logger logger = LoggerFactory.getLogger(File2TableSynchroJobConfig.class);

	@Value("${application.file2tablesynchro-step.chunksize:10}")
	private int chunkSize;

	@Autowired
	private DataSource dataSource;

	/**
	 * @param file2TableSynchroStep the injected Step bean
	 * @return the job bean
	 */
	@Bean
	public Job file2TableSynchroJob(final Step file2TableSynchroStep /* injected by Spring */) {
		return jobBuilderFactory.get("file2tablesynchro-job") //
				.incrementer(new RunIdIncrementer()) // job can be launched as many times as desired
				.validator(new DefaultJobParametersValidator(new String[] { "customer-file", "output-file" },
						new String[] {})) //
				.start(file2TableSynchroStep) //
				.listener(reportListener()) //
				.build();
	}

	/**
	 * @param masterDetailReader the injected {@link MasterDetailReader} bean
	 * @param customerWriter     the injected {@link FlatFileItemWriter} bean
	 * @return a Step bean
	 */
	@Bean
	public Step file2TableSynchroStep(final CompositeAggregateReader<Customer, Transaction, Long> masterDetailReader,
			final ItemWriter<? super Customer> customerWriter /* injected by Spring */) {

		return stepBuilderFactory.get("file2tablesynchro-step") //
				.<Customer, Customer>chunk(chunkSize) //
				.reader(masterDetailReader) //
				.processor(processor()) //
				.writer(customerWriter) //
				.listener(reportListener()) //
				.build();
	}

	/**
	 * @param customerFile the injected customer file job parameter
	 * @return a {@link FlatFileItemReader} bean
	 */
	@StepScope // Mandatory for using jobParameters
	@Bean
	public FlatFileItemReader<Customer> customerReader(
			@Value("#{jobParameters['customer-file']}") final String customerFile) {

		return new FlatFileItemReaderBuilder<Customer>() //
				.name("customerReader") //
				.resource(new FileSystemResource(customerFile)) //
				.delimited() //
				.delimiter(";") //
				.names("number", "firstName", "lastName", "address", "city", "state", "postCode") //
				.linesToSkip(1) //
				.fieldSetMapper(new BeanWrapperFieldSetMapper<Customer>() {
					{
						setTargetType(Customer.class);
					}
				}).build();
	}

	/**
	 * @return a {@link JdbcCursorItemReader} bean
	 */
	//	@Bean
	//	public JdbcCursorItemReader<Transaction> transactionReader() {
	//
	//		return new JdbcCursorItemReaderBuilder<Transaction>() //
	//				.dataSource(dataSource) //
	//				.name("transactionReader") //
	//				.sql("SELECT * FROM TRANSACTION ORDER BY CUSTOMER_NUMBER") //
	//				.rowMapper((rs, rowNum) -> {
	//					final Transaction transaction = new Transaction();
	//					transaction.setCustomerNumber(rs.getLong("CUSTOMER_NUMBER"));
	//					transaction.setNumber(rs.getString("NUMBER"));
	//					transaction.setTransactionDate(rs.getDate("TRANSACTION_DATE").toLocalDate());
	//					transaction.setAmount(rs.getDouble("AMOUNT"));
	//					return transaction;
	//				}).build();
	//	}


	@Bean
	public JdbcPagingItemReader<Transaction> transactionReader(final DataSource dataSource,
			final PagingQueryProvider queryProvider) {

		return new JdbcPagingItemReaderBuilder<Transaction>() //
				.name("transactionReader") //
				.dataSource(dataSource) //
				.pageSize(100) //
				.queryProvider(queryProvider)//
				.rowMapper(new BeanPropertyRowMapper<>(Transaction.class)) //
				.build();

	}

	@Bean
	public SqlPagingQueryProviderFactoryBean queryProvider(final DataSource dataSource) {
		final SqlPagingQueryProviderFactoryBean provider = new SqlPagingQueryProviderFactoryBean();

		provider.setDataSource(dataSource);
		provider.setSelectClause("SELECT customer_number, number, amount, transaction_date");
		provider.setFromClause("FROM Transaction");
		provider.setSortKeys(sortByCustomerNumberAsc());

		return provider;
	}

	private Map<String, Order> sortByCustomerNumberAsc() {
		final Map<String, Order> sortConfiguration = new LinkedHashMap<>();
		sortConfiguration.put("customer_number", Order.ASCENDING);
		return sortConfiguration;
	}

	/**
	 * Processor that sum customer's transactions to compute his balance.
	 *
	 * @return the processor
	 */
	private ItemProcessor<Customer, Customer> processor() {
		return new ItemProcessor<Customer, Customer>() {
			@Override
			public Customer process(final Customer customer) {
				final double sum = customer.getTransactions().stream().mapToDouble(x -> x.getAmount()).sum();
				customer.setBalance(new BigDecimal(sum).setScale(2, RoundingMode.HALF_UP).doubleValue());
				logger.debug(customer.toString());
				return customer;
			}
		};
	}

	/**
	 * @param outputFile the injected output file job parameter
	 * @return a {@link FlatFileItemWriter} bean
	 */
	@StepScope // Mandatory for using jobParameters
	@Bean
	public FlatFileItemWriter<Customer> customerWriter(
			@Value("#{jobParameters['output-file']}") final String outputFile) {

		return new FlatFileItemWriterBuilder<Customer>().name("customerWriter")
				.resource(new FileSystemResource(outputFile)) //
				.delimited() //
				.delimiter(";") //
				.names("number", "firstName", "lastName", "address", "city", "state", "postCode", "balance") //
				.build();

	}

}
