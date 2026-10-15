package fr.training.springbatch.tools.decider;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.step.StepExecution;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

public class BusinessDayDecider implements JobExecutionDecider {

    private static final Logger LOGGER = LoggerFactory.getLogger(BusinessDayDecider.class);

    private final Clock clock;

    public BusinessDayDecider() {
        this(Clock.system(ZoneId.of("Europe/Paris")));
    }

    public BusinessDayDecider(Clock clock) {
        this.clock = clock;
    }

    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, @Nullable StepExecution stepExecution) {
        LocalDate today = LocalDate.now(clock);
        if (FrenchHolidaysUtil.isBusinessDay(today)) {
            LOGGER.info("{} is business day : CONTINUE", today);
            return new FlowExecutionStatus("CONTINUE");
        }
        LOGGER.info("{} isn't business day : STOP", today);
        return FlowExecutionStatus.COMPLETED;
    }

}
