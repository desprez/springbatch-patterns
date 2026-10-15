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

/**
 * A Spring Batch {@link JobExecutionDecider} that gates job execution on whether the current
 * date is a French working day.
 *
 * <p>A <em>working day</em> is any day from Monday to Friday that is not a French public
 * holiday (as defined by {@link FrenchHolidaysUtil#isWorkingDay(LocalDate)}).
 * Weekends and public holidays are considered non-working days.
 *
 * <p>The decider returns:
 * <ul>
 *   <li>{@code "CONTINUE"} — when today is a working day;</li>
 *   <li>{@link FlowExecutionStatus#COMPLETED} — when today is a weekend day or a public holiday.</li>
 * </ul>
 *
 * <p>The clock used to obtain the current date can be injected via the
 * {@link #WorkingDayDecider(Clock)} constructor, making the class fully testable without
 * relying on the system clock.
 *
 * <p><strong>Usage example</strong> — declare the decider as a Spring bean and wire it into
 * a flow that skips processing on weekends and public holidays:
 * <pre>{@code
 * @Bean
 * public WorkingDayDecider workingDayDecider() {
 *     return new WorkingDayDecider();
 * }
 *
 * @Bean
 * public Job myJob(JobRepository jobRepository,
 *                  WorkingDayDecider workingDayDecider,
 *                  Step processStep) {
 *     return new JobBuilder("myJob", jobRepository)
 *             .start(workingDayDecider)
 *                 .on("CONTINUE").to(processStep)
 *             .from(workingDayDecider)
 *                 .on("*").end()        // COMPLETED on weekend / holiday — skip silently
 *             .end()
 *             .build();
 * }
 * }</pre>
 *
 * @see BusinessDayDecider for a variant that also counts Saturday as a valid business day
 */
public class WorkingDayDecider implements JobExecutionDecider {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkingDayDecider.class);

    private final Clock clock;

    /**
     * Creates a decider using the system clock in the {@code Europe/Paris} time zone.
     */
    public WorkingDayDecider() {
        this(Clock.system(ZoneId.of("Europe/Paris")));
    }

    /**
     * Creates a decider using the supplied {@code clock}.
     * <p>Use this constructor in tests to inject a fixed clock and pin the current date.
     *
     * @param clock the clock used to obtain today's date; must not be {@code null}
     */
    public WorkingDayDecider(Clock clock) {
        this.clock = clock;
    }

    /**
     * Decides whether the job should continue based on the current date.
     *
     * @param jobExecution  the current job execution
     * @param stepExecution the last step execution, or {@code null} if no step has run yet
     * @return {@code "CONTINUE"} if today is a working day, {@link FlowExecutionStatus#COMPLETED} otherwise
     */
    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, @Nullable StepExecution stepExecution) {
        LocalDate today = LocalDate.now(clock);
        if (FrenchHolidaysUtil.isWorkingDay(today)) {
            LOGGER.info("{} is working day : CONTINUE", today);
            return new FlowExecutionStatus("CONTINUE");
        }
        String holidayName = FrenchHolidaysUtil.getHolidayName(today);
        LOGGER.info("{} is {}, isn't working day : STOP", today, holidayName);
        return FlowExecutionStatus.COMPLETED;
    }

}
