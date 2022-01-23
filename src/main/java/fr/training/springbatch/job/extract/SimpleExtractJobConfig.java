package fr.training.springbatch.job.extract;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileHeaderCallback;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.util.StringUtils;

import fr.training.springbatch.app.dto.Transaction;
import fr.training.springbatch.app.job.AbstractJobConfiguration;

/**
 *
 */
public class SimpleExtractJobConfig extends AbstractJobConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(SimpleExtractJobConfig.class);

	private static final String FILENAME = "simple-extract.csv";

	@Value("${application.simple-extract-step.chunksize:10}")
	private int chunkSize;

	@Autowired
	public DataSource dataSource;

	@Bean
	public Job simpleExtractJob(final Step extractStep) {
		return jobBuilderFactory.get("simple-extract-job") //
				.validator(new DefaultJobParametersValidator(new String[] { "output-dir" }, new String[] {}))
				.incrementer(new RunIdIncrementer()) //
				.flow(extractStep) //
				.end() //
				.listener(reportListener()) //
				.build();
	}

	@Bean
	public Step extractStep(final FlatFileItemWriter<Transaction> extractWriter) {
		return stepBuilderFactory.get("simple-extract-step") //
				.<Transaction, Transaction>chunk(chunkSize) //
				.reader(simpleExtractReader()) //
				.processor(simpleExtractProcessor()) //
				.writer(extractWriter) //
				.listener(progressListener()) //
				.build();
	}

	/**
	 * ItemReader is an abstract representation of how data is provided as input to
	 * a Step. When the inputs are exhausted, the ItemReader returns null.
	 */
	@Bean
	public JdbcCursorItemReader<Transaction> simpleExtractReader() {

		return new JdbcCursorItemReaderBuilder<Transaction>() //
				.name("simpleExtractReader") //
				.dataSource(dataSource) //
				.sql("SELECT customer_number, number, amount, transaction_date FROM Transaction ORDER BY customer_number, number ASC") //
				.rowMapper(new BeanPropertyRowMapper<>(Transaction.class)) //
				.build();
	}

	/**
	 * ItemProcessor represents the business processing of an item. The data read by
	 * ItemReader can be passed on to ItemProcessor. In this unit, the data is
	 * transformed and sent for writing. If, while processing the item, it becomes
	 * invalid for further processing, you can return null. The nulls are not
	 * written by ItemWriter.
	 */
	@Bean
	public ItemProcessor<Transaction, Transaction> simpleExtractProcessor() {
		return new ItemProcessor<Transaction, Transaction>() {

			@Override
			public Transaction process(final Transaction transaction) throws Exception {
				logger.debug("Processing {}", transaction);
				return transaction;
			}
		};
	}

	/**
	 * ItemWriter is the output of a Step.
	 *
	 * @param incrementalFilename spring injected resource
	 */
	@Bean
	public FlatFileItemWriter<Transaction> simpleExtractWriter(final Resource incrementalFilename) {

		return new FlatFileItemWriterBuilder<Transaction>() //
				.name("simpleExtractWriter") //
				.resource(incrementalFilename) //
				.delimited() //
				.delimiter(";") //
				.names("customerNumber", "number", "transactionDate", "amount") //
				.headerCallback(new FlatFileHeaderCallback() {
					@Override
					public void writeHeader(final Writer writer) throws IOException {
						writer.write("customerNumber;number;transactionDate;amount");
					}
				}) //
				.build();
	}

	/**
	 * Get an unique filename resource according to a file name and a job unique run
	 * identifier.
	 *
	 * <b>Must be used in conjunction with RunIdIncrementer</b>
	 *
	 * @param outputdir the output directory job parameter
	 * @param runId     the job run id
	 * @return a Resource Bean according to de
	 */
	@StepScope // Mandatory for using jobParameters
	@Bean
	public Resource incrementalFilename(@Value("#{jobParameters['output-dir']}") final String outputdir,
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
