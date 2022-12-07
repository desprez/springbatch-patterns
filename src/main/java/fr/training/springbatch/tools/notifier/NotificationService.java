package fr.training.springbatch.tools.notifier;

import org.springframework.batch.core.JobExecution;

/**
 *
 */
public interface NotificationService {

    void notify(JobExecution jobExecution);

}
