package fr.training.springbatch.controlbreakjob;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.io.FileSystemResource;

import fr.training.springbatch.app.dto.Customer;
import fr.training.springbatch.app.dto.Transaction;
import fr.training.springbatch.app.dto.TransactionSum;
import fr.training.springbatch.common.AbstractJobConfiguration;

/**
 * This job groups all transactions by customer number and exports result to csv
 * file.
 *
 * It use an {@link ItemListPeekableItemReader} used to get a list of
 * {@link Transaction} grouped by {@link Customer}
 *
 * @author Desprez
 */
@Configuration
public class ControlBreakJobConfig extends AbstractJobConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(ControlBreakJobConfig.class);

	@Bean
	public Job controlBreakJob(final Step controlBreakStep /* injected by Spring */) {
		return jobBuilderFactory.get("controlbreak-job") //
				.incrementer(new RunIdIncrementer()) // job can be launched as many times as desired
				.validator(new DefaultJobParametersValidator(new String[] { "transaction-file", "output-file" },
						new String[] {})) //
				.start(controlBreakStep) //
				.listener(reportListener()) //
				.build();
	}

	@Bean
	public Step controlBreakStep(final ItemListPeekableItemReader<Transaction> controlBreakReader,
			final ItemWriter<TransactionSum> transactionSumWriter /* injected by Spring */) {

		return stepBuilderFactory.get("controlbreak-step") //
				.<List<Transaction>, TransactionSum>chunk(15) //
				.reader(controlBreakReader) //
				.processor(processor()) //
				.writer(transactionSumWriter) //
				.listener(reportListener()) //
				.build();
	}

	@Bean(destroyMethod = "")
	public ItemListPeekableItemReader<Transaction> controlBreakReader(
			final FlatFileItemReader<Transaction> transactionReader) {

		final ItemListPeekableItemReader<Transaction> groupReader = new ItemListPeekableItemReader<Transaction>();
		groupReader.setDelegate(transactionReader);
		groupReader.setBreakKeyStrategy(new BreakKeyStrategy<Transaction>() {
			@Override
			public boolean isSameGroup(final Transaction transaction1, final Transaction transaction2) {
				return transaction1.getCustomerNumber().equals(transaction2.getCustomerNumber());
			}
		});
		return groupReader;
	}

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
						setConversionService(createConversionService());
					}
				}).build();
	}

	/**
	 * Converter to parse local date
	 */
	public ConversionService createConversionService() {
		final DefaultConversionService conversionService = new DefaultConversionService();
		DefaultConversionService.addDefaultConverters(conversionService);
		conversionService.addConverter(new Converter<String, LocalDate>() {
			@Override
			public LocalDate convert(final String text) {
				final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
				return LocalDate.parse(text, formatter);
			}
		});
		return conversionService;
	}

	private ItemProcessor<List<Transaction>, TransactionSum> processor() {
		return new ItemProcessor<List<Transaction>, TransactionSum>() {
			@Override
			public TransactionSum process(final List<Transaction> items) throws Exception {
				final TransactionSum transactionSum = new TransactionSum();
				final double sum = items.stream().mapToDouble(x -> x.getAmount()).sum();
				transactionSum.setCustomerNumber(items.get(0).getCustomerNumber());
				transactionSum.setBalance(new BigDecimal(sum).setScale(2, RoundingMode.HALF_UP).doubleValue());
				logger.info(transactionSum.toString());
				return transactionSum;
			}
		};
	}

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
