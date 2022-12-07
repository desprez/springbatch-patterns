package fr.training.springbatch.job.complexml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.batch.item.xml.builder.StaxEventItemReaderBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.util.StreamUtils;

import fr.training.springbatch.app.job.AbstractJobConfiguration;
import fr.training.springbatch.job.complexml.model.OperationOrph;
import fr.training.springbatch.job.complexml.model.Record;
import fr.training.springbatch.job.complexml.model.RemiseBancaire;
import fr.training.springbatch.tools.writer.NoOpWriter;

/**
 *
 * @author Desprez
 */
public class ComplexmlJobConfig extends AbstractJobConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(ComplexmlJobConfig.class);

	@Value("${application.complexml-step.chunksize:10}")
	private int chunkSize;

	@Bean
	public Job complexmlJob(final Step fixXmlFileStep, final Step complexmlStep /* injected by Spring */) {
		return jobBuilderFactory.get("complex-job") //
				.incrementer(new RunIdIncrementer()) // job can be launched as many times as desired
				.validator(new DefaultJobParametersValidator(new String[] { "xml-file" }, new String[] {})) //
				.start(fixXmlFileStep) //
				.next(complexmlStep).listener(reportListener()) //
				.build();
	}

	@JobScope // Mandatory for using jobParameters
	@Bean
	public Step fixXmlFileStep(@Value("#{jobParameters['xml-file']}") final String xmlFile /* injected by Spring */) {
		return stepBuilderFactory.get("fixXmlFile-step") //
				.tasklet(new Tasklet() {
					@Override
					public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext)
							throws Exception {

						final FileInputStream fis = new FileInputStream(xmlFile);

						final List<InputStream> streams = Arrays.asList(new ByteArrayInputStream("<root>".getBytes()),
								fis, new ByteArrayInputStream("</root>".getBytes()));

						final File workFile = File.createTempFile("input", ".xml");

						StreamUtils.copy(new SequenceInputStream(Collections.enumeration(streams)),
								new FileOutputStream(workFile));

						// Put file path to job execution context to share it with others steps
						chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext()
						.put("workfile", workFile.getAbsoluteFile());

						logger.info("work file path is {}", workFile.getAbsoluteFile());

						return RepeatStatus.FINISHED;
					}
				}).listener(reportListener()) //
				.build();
	}

	/**
	 * @param complexmlReader
	 * @return a Step Bean
	 */
	@Bean
	public Step complexmlStep(final StaxEventItemReader<Record> complexmlReader) {

		return stepBuilderFactory.get("complexml-step") //
				.<Record, Record>chunk(chunkSize) //
				.reader(complexmlReader) //
				.processor(processor()) //
				.writer(complexmlWriter()) //
				.listener(reportListener()) //
				.build();
	}

	@JobScope // Mandatory for using jobParameters
	@Bean
	public StaxEventItemReader<Record> complexmlReader(
			@Value("#{jobExecutionContext['workfile']}") final String workFile /* injected by Spring */) {
		return new StaxEventItemReaderBuilder<Record>() //
				.name("itemReader") //
				.resource(new FileSystemResource(workFile)) //
				.addFragmentRootElements("RemiseBancaire", "OperationOrph") //
				.unmarshaller(jaxbMarshaller()) //
				.build();

	}

	@Bean
	public Jaxb2Marshaller jaxbMarshaller() {
		final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
		marshaller.setClassesToBeBound(RemiseBancaire.class, OperationOrph.class);
		return marshaller;
	}

	@Bean
	public ItemProcessor<Record, Record> processor() {
		return new ItemProcessor<Record, Record>() {

			@Override
			public Record process(final Record item) throws Exception {

				if (item instanceof RemiseBancaire) {
					final RemiseBancaire rb = (RemiseBancaire) item;
					logger.info("Processing {}", rb);
				}
				if (item instanceof OperationOrph) {
					final OperationOrph oo = (OperationOrph) item;
					logger.info("Processing {}", oo);
				}
				return item;
			}
		};
	}

	private NoOpWriter<Record> complexmlWriter() {
		return new NoOpWriter<Record>();
	}

}
