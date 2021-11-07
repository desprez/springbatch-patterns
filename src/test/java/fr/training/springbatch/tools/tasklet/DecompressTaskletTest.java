package fr.training.springbatch.tools.tasklet;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.core.io.ClassPathResource;

/**
 * Test upon DecompressTasklet class
 */
public class DecompressTaskletTest {

	private static final String[] EXPECTED_CONTENT = new String[] { //
			"PRODUCT_ID,NAME,DESCRIPTION,PRICE", //
			"PR....210,BlackBerry 8100 Pearl,,124.60", //
			"PR....211,Sony Ericsson W810i,,139.45", //
			"PR....212,Samsung MM-A900M Ace,,97.80", //
			"PR....213,Toshiba M285-E 14,,166.20", //
			"PR....214,Nokia 2610 Phone,,145.50", //
			"PR....215,CN Clogs Beach/Garden Clog,,190.70", //
			"PR....216,AT&T 8525 PDA,,289.20", //
			"PR....217,Canon Digital Rebel XT 8MP Digital SLR Camera,,13.70", //
	};

	@Test
	public void execute_with_valide_archive_should_success() throws Exception {
		// Given
		final StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();
		final StepContribution contribution = new StepContribution(stepExecution);
		final ChunkContext context = new ChunkContext(new StepContext(stepExecution));

		final DecompressTasklet tasklet = new DecompressTasklet();

		tasklet.setInputResource(new ClassPathResource("/input/products.zip"));
		final File outputDir = new File("./target/decompresstasklet");
		if (outputDir.exists()) {
			FileUtils.deleteDirectory(outputDir);
		}
		tasklet.setTargetDirectory(outputDir.getAbsolutePath());
		tasklet.setTargetFile("products.txt");

		// When
		tasklet.execute(contribution, context);

		// Then
		final File output = new File(outputDir, "products.txt");
		assertThat(output.exists(), is(true));
		assertThat(FileUtils.readLines(output, Charset.defaultCharset()).toArray(), equalTo(EXPECTED_CONTENT));
	}

	@Test(expected = IllegalStateException.class) // Then
	public void execute_with_corrupted_archive_should_fail() throws Exception {
		// Given
		final StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();
		final StepContribution contribution = new StepContribution(stepExecution);
		final ChunkContext context = new ChunkContext(new StepContext(stepExecution));

		final DecompressTasklet tasklet = new DecompressTasklet();

		tasklet.setInputResource(new ClassPathResource("/input/products_corrupted.zip"));
		final File outputDir = new File("./target/decompresstasklet");
		if (outputDir.exists()) {
			FileUtils.deleteDirectory(outputDir);
		}
		tasklet.setTargetDirectory(outputDir.getAbsolutePath());
		tasklet.setTargetFile("products.txt");

		// When
		tasklet.execute(contribution, context);
	}
}