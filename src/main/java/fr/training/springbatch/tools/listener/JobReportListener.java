package fr.training.springbatch.tools.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

public class JobReportListener implements JobExecutionListener, StepExecutionListener {

	private static final Logger logger = LoggerFactory.getLogger(JobReportListener.class);

	@Override
	public void beforeJob(final JobExecution jobExecution) {
		logger.info("{} STARTING...", jobExecution.getJobInstance().getJobName());
	}

	@Override
	public void afterJob(final JobExecution jobExecution) {
		logger.info("{} ENDING", jobExecution.getJobInstance().getJobName());
		for (final StepExecution stepExecution : jobExecution.getStepExecutions()) {
			logger.info(stepExecution.getSummary().replace(" ", "\n") );
		}
	}

	@Override
	public void beforeStep(final StepExecution stepExecution) {
		logger.info(stepExecution.getSummary());
	}

	@Override
	public ExitStatus afterStep(final StepExecution stepExecution) {
		logger.info(stepExecution.getSummary());
		return stepExecution.getExitStatus();
	}

}
