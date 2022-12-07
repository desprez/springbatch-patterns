package fr.training.springbatch.tools.chaos.tasklet;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import fr.training.springbatch.tools.chaos.BatchChaosException;

/**
 * Tasklet used to mimimic unstable process by providing exception one in two.
 *
 * Useful for chaos testing on cloud.
 *
 * @author Desprez
 */
public class BipolarTasklet implements Tasklet {

    @Override
    public RepeatStatus execute(final StepContribution stepContribution, final ChunkContext chunkContext) throws Exception {

        final Long runId = chunkContext.getStepContext().getJobInstanceId();

        if (runId % 2 == 0) {
            throw new BatchChaosException("BipolarTasklet expected fail");
        }

        return RepeatStatus.FINISHED;
    }
}
