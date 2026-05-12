package fr.training.springbatch.tools.tasklet;

import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;

import java.io.File;
import java.util.Arrays;

public class MoveFilesTasklet implements Tasklet {

    private final String filePath = "someFilePAth";

    @Override
    public RepeatStatus execute(final StepContribution stepContribution, final ChunkContext chunkContext) throws Exception {

        final File directory = new File(filePath);
        Arrays.asList(directory.listFiles((dir, name) -> name.matches("yourfilePrefix.*?"))).stream()
                .forEach(singleFile -> singleFile.renameTo(new File("someNewFilePath")));

        return RepeatStatus.FINISHED;
    }

}