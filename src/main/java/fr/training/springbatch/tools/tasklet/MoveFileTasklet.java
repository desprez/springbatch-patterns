package fr.training.springbatch.tools.tasklet;

import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.springframework.util.Assert.notNull;

/**
 * A {link Tasklet} that move file from a given source directory to a target directory.
 */
public class MoveFileTasklet implements Tasklet {

    private String filename;

    private String sourceDirectory;

    private String targetDirectory;

    @Override
    public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {
        checkParameters();

        final Path destinationPath = Path.of(targetDirectory);
        if (!Files.isDirectory(destinationPath)) {
            Files.createDirectory(destinationPath);
        }

        Files.move(Path.of(sourceDirectory + filename), Path.of(targetDirectory + filename), StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE);

        contribution.incrementWriteCount(1);

        return RepeatStatus.FINISHED;
    }

    public void checkParameters() {
        notNull(filename, "filename is required");
        notNull(sourceDirectory, "sourceDirectory is required");
        notNull(targetDirectory, "targetDirectory is required");
    }

    public void setFilename(final String filename) {
        this.filename = filename;
    }

    public void setSourceDirectory(final String sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
    }

    public void setTargetDirectory(final String targetDirectory) {
        this.targetDirectory = targetDirectory;
    }

}
