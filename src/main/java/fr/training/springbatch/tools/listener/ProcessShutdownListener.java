package fr.training.springbatch.tools.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;

/**
 * This class listens to events from the Operating System requesting the Batch to shutdown. For example when the user hits CTRL-C or the system is shutting
 * down. If we ignore these signals the jobs will be left hanging. This class attempts to remedy this situation by requesting the JobOperator to gracefully stop
 * a job when the JVM calls the shutdown hook.
 */
public class ProcessShutdownListener implements JobExecutionListener {
    private static final Logger logger = LoggerFactory.getLogger(ProcessShutdownListener.class);

    private JobOperator jobOperator;

    @Override
    public void afterJob(final JobExecution jobExecution) {
        /* do nothing. */
    }

    @Override
    public void beforeJob(final JobExecution jobExecution) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    jobOperator.stop(jobExecution.getId());
                    while (jobExecution.isRunning()) {
                        logger.info("waiting for job to stop...");
                        try {
                            Thread.sleep(100);
                        } catch (final InterruptedException e) {
                        }
                    }
                } catch (NoSuchJobExecutionException | JobExecutionNotRunningException e1) {
                    logger.error(e1.getMessage());
                }
            }
        });
    }

    public void setJobOperator(final JobOperator jobOperator) {
        this.jobOperator = jobOperator;
    }

}