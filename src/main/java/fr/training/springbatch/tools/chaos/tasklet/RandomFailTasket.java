package fr.training.springbatch.tools.chaos.tasklet;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

/**
 * Tasklet used to mimimic unstable process.
 *
 * Useful for chaos testing on cloud.
 *
 * @author Desprez
 */
public class RandomFailTasket implements Tasklet {

	@Override
	public RepeatStatus execute(final StepContribution stepContribution, final ChunkContext chunkContext)
			throws Exception {

		if (Math.random() < 0.5) {
			throw new BatchChaosException("RandomFailTasket expected fail");
		}

		return RepeatStatus.FINISHED;
	}
}
