package fr.training.springbatch.tools.notifier;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;

public class JobMonitoringListener extends JobExecutionListenerSupport {

	private final NotificationService notifier;

	public JobMonitoringListener(final NotificationService notifier) {
		this.notifier = notifier;
	}

	@Override
	public void afterJob(final JobExecution jobExecution) {
		if (jobExecution.getStatus() == BatchStatus.FAILED) {
			notifier.notify(jobExecution);
		}
	}

}
