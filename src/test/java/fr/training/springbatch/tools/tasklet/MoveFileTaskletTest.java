package fr.training.springbatch.tools.tasklet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.test.MetaDataInstanceFactory;

class MoveFileTaskletTest {

	private static File outputDir = new File("target/movefiletasklet/");

	@BeforeEach
	public void cleanup() throws IOException {
		if (outputDir.exists()) {
			FileUtils.deleteDirectory(outputDir);
		}
		FileUtils.writeStringToFile(new File("target/file2move.txt"), "This is a test", Charset.defaultCharset());
	}

	@Test
	void execute_with_existing_file_should_success() throws Exception {
		// Given
		final StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();
		final StepContribution contribution = new StepContribution(stepExecution);
		final ChunkContext context = new ChunkContext(new StepContext(stepExecution));

		final MoveFileTasklet tasklet = new MoveFileTasklet();
		tasklet.setFilename("file2move.txt");
		tasklet.setSourceDirectory("target/");
		tasklet.setTargetDirectory("target/movefiletasklet/");

		// When
		final RepeatStatus status = tasklet.execute(contribution, context);

		// Then
		assertThat(status, equalTo(RepeatStatus.FINISHED));

		assertThat(new File("target/movefiletasklet/file2move.txt")).exists();
		assertThat(new File("target/file2move.txt")).doesNotExist();
	}
}
