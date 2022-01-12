package fr.training.springbatch.tools.chaos.tasklet;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

/**
 * Tasklet used to mimimic long duration process. Display countdown.
 *
 * Useful for chaos testing on cloud.
 *
 * @author Desprez
 */
public class WaitingTasklet implements Tasklet {

	private static final Logger logger = LoggerFactory.getLogger(WaitingTasklet.class);

	private static final int ONE_SECOND = 1000;

	private Duration duration = Duration.ofSeconds(1); // Default 1s

	private String waitingMessage = "WaitingTasklet is waiting for {} second";

	@Override
	public RepeatStatus execute(final StepContribution stepContribution, final ChunkContext chunkContext)
			throws Exception {
		logger.info(waitingMessage, duration);
		for (Long i = duration.getSeconds(); i >= 0; i--) {
			if (i == 0) {
				logger.info("GO");
			} else {
				logger.info("{}", i);
			}
			Thread.sleep(ONE_SECOND);
		}
		return RepeatStatus.FINISHED;
	}

	public void setDuration(final Duration seconds) {
		this.duration = seconds;
	}

	public void setWaitingMessage(final String waitingMessage) {
		this.waitingMessage = waitingMessage;
	}

}