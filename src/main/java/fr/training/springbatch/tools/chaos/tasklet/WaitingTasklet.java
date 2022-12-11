package fr.training.springbatch.tools.chaos.tasklet;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;

/**
 * Tasklet used to mimimic long duration process. Display countdown.
 *
 * Useful for chaos testing on cloud.
 *
 * @author Desprez
 */
public class WaitingTasklet implements StoppableTasklet, InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(WaitingTasklet.class);

    private static final int ONE_SECOND = 1000;

    private String waitingMessage = "WaitingTasklet is waiting for {} second";

    private Duration duration = Duration.ofSeconds(1); // Default 1s

    private long i;

    private boolean stopped;

    public WaitingTasklet(final Duration duration) throws Exception {
        this.duration = duration;
        afterPropertiesSet();
    }

    @Override
    public RepeatStatus execute(final StepContribution stepContribution, final ChunkContext chunkContext) throws Exception {
        log.info(waitingMessage, duration.getSeconds());

        Thread.sleep(ONE_SECOND);
        i--;

        if (i == 0) {
            log.info("GO");
            return RepeatStatus.FINISHED;
        } else {
            log.info("{}", i);
            return RepeatStatus.continueIf(!stopped);
        }
    }

    @Override
    public void stop() {
        log.info("Stop requested");
        stopped = true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        i = duration.getSeconds();
    }

    public void setDuration(final Duration seconds) {
        duration = seconds;
    }

    public void setWaitingMessage(final String waitingMessage) {
        this.waitingMessage = waitingMessage;
    }

}