package fr.training.springbatch.tools.listener;

import java.util.function.LongSupplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.listener.ChunkListenerSupport;
import org.springframework.batch.core.scope.context.ChunkContext;

/**
 *
 */
public class ProgressListener extends ChunkListenerSupport implements JobExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(ProgressListener.class);

    private LongSupplier totalRecordsSupplier;

    @Override
    public void beforeJob(final JobExecution jobExecution) {

        final long total = totalRecordsSupplier.getAsLong();

        jobExecution.getExecutionContext().put("JobComplete", total);
    }

    public Long getProgress(final JobExecution jobExecution) {
        final double jobComplete = (Double) jobExecution.getExecutionContext().get("jobComplete");
        double reads = 0;
        for (final StepExecution step : jobExecution.getStepExecutions()) {
            reads = reads + step.getReadCount();
        }
        return Math.round(reads / jobComplete * 100);
    }

    @Override
    public void afterChunk(final ChunkContext context) {
        log.info("Progress : {} %", getProgress(context.getStepContext().getStepExecution().getJobExecution()));
    }

    @Override
    public void afterJob(final JobExecution jobExecution) {
        // TODO Auto-generated method stub

    }
}
