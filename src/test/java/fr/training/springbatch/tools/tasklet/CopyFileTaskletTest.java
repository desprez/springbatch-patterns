package fr.training.springbatch.tools.tasklet;

import static org.assertj.core.api.Assertions.assertThat;

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

class CopyFileTaskletTest {

    private static File outputDir = new File("target/copyfiletasklet/");

    @BeforeEach
    void cleanup() throws IOException {
        if (outputDir.exists()) {
            FileUtils.deleteDirectory(outputDir);
        }
        FileUtils.writeStringToFile(new File("target/file2copy.txt"), "This is a test", Charset.defaultCharset());
    }

    @Test
    void execute_with_existing_file_should_success() throws Exception {
        // Given
        final StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();
        final StepContribution contribution = new StepContribution(stepExecution);
        final ChunkContext context = new ChunkContext(new StepContext(stepExecution));

        final CopyFileTasklet tasklet = new CopyFileTasklet();
        tasklet.setFilename("file2copy.txt");
        tasklet.setSourceDirectory("target/");
        tasklet.setTargetDirectory("target/copyfiletasklet/");

        // When
        final RepeatStatus status = tasklet.execute(contribution, context);

        // Then
        assertThat(status).isEqualTo(RepeatStatus.FINISHED);
        final File file = new File("target/copyfiletasklet/file2copy.txt");
        assertThat(file).exists();
    }

}
