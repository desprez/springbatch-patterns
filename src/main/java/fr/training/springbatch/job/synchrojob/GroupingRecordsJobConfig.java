package fr.training.springbatch.job.synchrojob;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;

import fr.training.springbatch.app.dto.Transaction;
import fr.training.springbatch.app.dto.TransactionSum;
import fr.training.springbatch.app.job.AbstractJobConfiguration;
import fr.training.springbatch.job.synchrojob.component.GroupReader;
import fr.training.springbatch.job.synchrojob.component.TransactionAccumulator;
import fr.training.springbatch.tools.synchro.ItemAccumulator;

/**
 * This job groups all transactions by customer number and exports result to csv
 * file using {@link ItemAccumulator} & {@link GroupReader}
 *
 * @author Desprez
 */
public class GroupingRecordsJobConfig extends AbstractJobConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(GroupingRecordsJobConfig.class);

	@Value("${application.groupingrecord-step.chunksize:10}")
	private int chunkSize;

	/**
	 * @param groupingRecordStep the injected Step bean
	 * @return the job bean
	 */
	@Bean
	public Job groupingRecordJob(final Step groupingRecordStep /* injected by Spring */) {
		return jobBuilderFactory.get("groupingrecord-job") //
				.incrementer(new RunIdIncrementer()) // job can be launched as many times as desired
				.validator(new DefaultJobParametersValidator(new String[] { "transaction-file", "output-file" },
						new String[] {})) //
				.start(groupingRecordStep) //
				.listener(reportListener()) //
				.build();
	}

	/**
	 * @param groupReader          the injected Transaction {@link GroupReader}
	 *                             bean.
	 * @param transactionSumWriter the injected TransactionSum ItemWriter
	 * @return a Step Bean
	 */
	@Bean
	public Step groupingRecordStep(final GroupReader<Transaction, String> groupReader,
			final ItemWriter<TransactionSum> transactionSumWriter) {

		return stepBuilderFactory.get("groupingrecord-step") //
				.<List<Transaction>, TransactionSum>chunk(chunkSize) //
				.reader(groupReader) //
				.processor(processor()) //
				.writer(transactionSumWriter) //
				.listener(reportListener()) //
				.build();
	}

	/**
	 * Delegate pattern reader
	 *
	 * @param transactionReader the injected Transaction {@link FlatFileItemReader}
	 *                          bean.
	 * @return a {@link GroupReader} bean
	 */
	@Bean(destroyMethod = "")
	public GroupReader<Transaction, String> groupReader(final FlatFileItemReader<Transaction> transactionReader) {

		final GroupReader<Transaction, String> groupReader = new GroupReader<Transaction, String>();
		groupReader.setAccumulator(new TransactionAccumulator(transactionReader));

		return groupReader;
	}

	/**
	 * @param transactionFile the injected transaction file job parameter
	 * @return a Transaction {@link FlatFileItemReader} bean
	 */
	@StepScope // Mandatory for using jobParameters
	@Bean
	public FlatFileItemReader<Transaction> transactionReader(
			@Value("#{jobParameters['transaction-file']}") final String transactionFile /* injected by Spring */) {

		return new FlatFileItemReaderBuilder<Transaction>() //
				.name("transactionReader") //
				.resource(new FileSystemResource(transactionFile)) //
				.delimited() //
				.delimiter(";") //
				.names(new String[] { "customerNumber", "number", "transactionDate", "amount" }) //
				.linesToSkip(1) //
				.fieldSetMapper(new BeanWrapperFieldSetMapper<Transaction>() {
					{
						setTargetType(Transaction.class);
						setConversionService(localDateConverter());
					}
				}).build();
	}

	/**
	 * Processor that sum customer's transactions to compute his balance, fill and
	 * return new TransactionSum objects.
	 *
	 * @return the processor
	 */
	private ItemProcessor<List<Transaction>, TransactionSum> processor() {
		return new ItemProcessor<List<Transaction>, TransactionSum>() {
			@Override
			public TransactionSum process(final List<Transaction> items) throws Exception {
				final TransactionSum transactionSum = new TransactionSum();
				final double sum = items.stream().mapToDouble(x -> x.getAmount()).sum();
				transactionSum.setCustomerNumber(items.get(0).getCustomerNumber());
				transactionSum.setBalance(new BigDecimal(sum).setScale(2, RoundingMode.HALF_UP).doubleValue());
				logger.debug(transactionSum.toString());
				return transactionSum;
			}
		};
	}

	/**
	 * @param outputFile the injected output file job parameter
	 * @return a TransactionSum {@link FlatFileItemReader} bean
	 */
	@StepScope // Mandatory for using jobParameters
	@Bean
	public FlatFileItemWriter<TransactionSum> transactionSumWriter(
			@Value("#{jobParameters['output-file']}") final String outputFile) {

		return new FlatFileItemWriterBuilder<TransactionSum>().name("transactionSumWriter")
				.resource(new FileSystemResource(outputFile)) //
				.delimited() //
				.delimiter(";") //
				.names(new String[] { "customerNumber", "balance" }) //
				.build();
	}

}
