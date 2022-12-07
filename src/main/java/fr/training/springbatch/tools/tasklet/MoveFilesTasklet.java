package fr.training.springbatch.tools.tasklet;

import java.io.File;
import java.util.Arrays;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

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