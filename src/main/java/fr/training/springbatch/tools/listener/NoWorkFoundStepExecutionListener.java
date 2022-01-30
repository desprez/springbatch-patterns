package fr.training.springbatch.tools.listener;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.listener.StepExecutionListenerSupport;

/**
 * Programmatically checks the metadata for no items processed and cause step failure.
 *
 * To be used when the absence of items to read is not a normal case
 */
public class NoWorkFoundStepExecutionListener extends StepExecutionListenerSupport {

	@Override
	public ExitStatus afterStep(final StepExecution stepExecution) {
		if (stepExecution.getReadCount() == 0) {
			return ExitStatus.FAILED;
		}
		return stepExecution.getExitStatus();
	}

}