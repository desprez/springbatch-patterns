package fr.training.springbatch.tools.listener;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.util.StopWatch;

public class ElapsedTimeJobListener implements JobExecutionListener {

	private final static Logger logger = LoggerFactory.getLogger(ElapsedTimeJobListener.class);

	private final StopWatch stopWatch = new StopWatch();

	@Override
	public void beforeJob(final JobExecution jobExecution) {
		logger.debug("Called beforeJob().");
		stopWatch.start();
	}

	@Override
	public void afterJob(final JobExecution jobExecution) {
		logger.debug("Called afterJob().");
		stopWatch.stop();
		logger.info("Elapsed time : {}", DurationFormatUtils.formatDurationHMS(stopWatch.getTotalTimeMillis()));
	}
}