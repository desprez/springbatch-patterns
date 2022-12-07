package fr.training.springbatch.tools.listener;

import static org.springframework.batch.core.metrics.BatchMetrics.calculateDuration;
import static org.springframework.batch.core.metrics.BatchMetrics.formatDuration;

import java.util.Iterator;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

/**
 * Listener used to log Job and Steps statistics.
 */
public class FullReportListener implements JobExecutionListener, StepExecutionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(FullReportListener.class);
    private static final String NEW_LINE = " \n";
    private static final String SEPARATOR_LINE = NEW_LINE + "+++++++++++++++++++++++++++++++++++++++++++++++++++++++";

    @Override
    public void afterJob(final JobExecution jobExecution) {
        final StringBuilder jobReport = new StringBuilder();
        jobReport.append(SEPARATOR_LINE + NEW_LINE);
        jobReport.append("Report for " + jobExecution.getJobInstance().getJobName() + NEW_LINE);
        jobReport.append("  Started     : " + jobExecution.getStartTime() + NEW_LINE);
        jobReport.append("  Finished    : " + jobExecution.getEndTime() + NEW_LINE);
        jobReport.append("  Exit-Code   : " + jobExecution.getExitStatus().getExitCode() + NEW_LINE);
        jobReport.append("  Exit-Descr. : " + jobExecution.getExitStatus().getExitDescription() + NEW_LINE);
        jobReport.append("  Status      : " + jobExecution.getStatus() + NEW_LINE);
        jobReport.append(SEPARATOR_LINE + NEW_LINE);

        jobReport.append("Job-Parameter:" + NEW_LINE);
        final JobParameters jp = jobExecution.getJobParameters();
        for (final Iterator<Entry<String, JobParameter>> iter = jp.getParameters().entrySet().iterator(); iter.hasNext();) {
            final Entry<String, JobParameter> entry = iter.next();
            jobReport.append("  " + entry.getKey() + "=" + entry.getValue() + NEW_LINE);
        }
        jobReport.append(" executed in ").append(formatDuration(calculateDuration(jobExecution.getStartTime(), jobExecution.getEndTime())));

        for (final StepExecution stepExecution : jobExecution.getStepExecutions()) {
            jobReport.append(logStep(stepExecution));
        }
        LOGGER.info(jobReport.toString());
    }

    /**
     * Get log statistics for one step execution.
     *
     * @param stepExecution
     *            step execution object
     * @return step statistics message
     */
    private String logStep(final StepExecution stepExecution) {

        final StringBuilder stepReport = new StringBuilder();
        stepReport.append(SEPARATOR_LINE + NEW_LINE);
        stepReport.append("Step [" + stepExecution.getStepName() + "]" + NEW_LINE);
        stepReport.append("Read count: " + stepExecution.getReadCount() + NEW_LINE);
        stepReport.append("Write count: " + stepExecution.getWriteCount() + NEW_LINE);
        stepReport.append("Commits: " + stepExecution.getCommitCount() + NEW_LINE);
        stepReport.append("Skip count: " + stepExecution.getSkipCount() + NEW_LINE);
        stepReport.append("Rollbacks: " + stepExecution.getRollbackCount() + NEW_LINE);
        stepReport.append("Filter: " + stepExecution.getFilterCount() + NEW_LINE);
        stepReport.append("Duration: " + formatDuration(calculateDuration(stepExecution.getStartTime(), stepExecution.getEndTime())));
        stepReport.append(SEPARATOR_LINE + NEW_LINE);

        return stepReport.toString();
    }

    @Override
    public void beforeJob(final JobExecution jobExecution) {
        LOGGER.info(SEPARATOR_LINE);
        LOGGER.info("{} STARTING...", jobExecution.getJobInstance().getJobName());
    }

    @Override
    public ExitStatus afterStep(final StepExecution stepExecution) {
        // LOGGER.info(SEPARATOR_LINE);
        LOGGER.info(logStep(stepExecution));
        return stepExecution.getExitStatus();
    }

    @Override
    public void beforeStep(final StepExecution stepExecution) {
        // nothing to do
    }
}