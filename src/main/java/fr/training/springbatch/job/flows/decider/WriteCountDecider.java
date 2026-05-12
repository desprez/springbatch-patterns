package fr.training.springbatch.job.flows.decider;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.util.Assert;

/**
 * Job execution decider that checks the records write number of the current step. If the records count is greater than 0, the fow continue else the flow is
 * stopped.
 */
public class WriteCountDecider implements JobExecutionDecider {

    private static final Logger logger = LoggerFactory.getLogger(WriteCountDecider.class);

    @Override
    public FlowExecutionStatus decide(final JobExecution jobExecution, @Nullable final StepExecution stepExecution) {
        Assert.notNull(stepExecution, "stepExecution must be not null");
        // Retrieve the record count from current Step execution
        final long recordCount = (int) stepExecution.getWriteCount();

        if (recordCount > 0) {
            logger.info("{} record(s) found : CONTINUE");
            return new FlowExecutionStatus("CONTINUE");
        }
        logger.info("No record found : STOP step NOW");
        return FlowExecutionStatus.COMPLETED;
    }

}
