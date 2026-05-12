package fr.training.springbatch.tools.tasklet;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class CompressTaskletTest {

    private static Resource csvFilesPath = new ClassPathResource("csv");
    private static File outputDir = new File("target/compresstasklet");

    @BeforeEach
    void createOutputDir() throws IOException {
        if (outputDir.exists()) {
            FileUtils.deleteDirectory(outputDir);
        }
    }

    @Test
    void execute_should_generate_archive_file() throws Exception {
        // Given
        final StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();
        final StepContribution contribution = new StepContribution(stepExecution);
        final ChunkContext context = new ChunkContext(new StepContext(stepExecution));

        final String archiveFilename = outputDir.getAbsolutePath() + "/archive.zip";
        final CompressTasklet tasklet = new CompressTasklet();
        tasklet.setBaseDirectoryToCompress(csvFilesPath.getFile().getAbsolutePath());
        tasklet.setArchiveFilename(archiveFilename);

        // When
        final RepeatStatus status = tasklet.execute(contribution, context);

        // Then
        assertThat(status).isEqualTo(RepeatStatus.FINISHED);
        final File file = new File(archiveFilename);
        assertThat(file).exists();
    }
}
