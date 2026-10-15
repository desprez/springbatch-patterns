package fr.training.springbatch.tools.decider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.test.MetaDataInstanceFactory;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class WorkingDayDeciderTest {

    // WorkingDay = Monday–Friday, excluding French public holidays

    private static Clock clockFor(LocalDate date) {
        return Clock.fixed(date.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC);
    }

    private static JobExecution jobExecution() {
        return MetaDataInstanceFactory.createJobExecution();
    }

    @Test
    void decide_returns_CONTINUE_on_regular_monday() {
        // Monday 2024-03-11
        WorkingDayDecider decider = new WorkingDayDecider(clockFor(LocalDate.of(2024, 3, 11)));

        FlowExecutionStatus status = decider.decide(jobExecution(), null);

        assertEquals("CONTINUE", status.getName());
    }

    @ParameterizedTest(name = "{2}")
    @CsvSource({
        "2024-03-16, COMPLETED, Saturday is not a working day",
        "2024-03-17, COMPLETED, Sunday is not a working day",
        "2024-05-01, COMPLETED, Fete du travail (Wednesday) is a holiday",
        "2024-12-25, COMPLETED, Noel (Wednesday) is a holiday"
    })
    void decide_returns_COMPLETED_on_non_working_day(LocalDate date, String expectedStatus, String description) {
        WorkingDayDecider decider = new WorkingDayDecider(clockFor(date));

        FlowExecutionStatus status = decider.decide(jobExecution(), null);

        assertEquals(expectedStatus, status.getName());
    }

    @Test
    void decide_returns_CONTINUE_on_regular_friday() {
        // Friday 2024-03-15
        WorkingDayDecider decider = new WorkingDayDecider(clockFor(LocalDate.of(2024, 3, 15)));

        FlowExecutionStatus status = decider.decide(jobExecution(), null);

        assertEquals("CONTINUE", status.getName());
    }

    @Test
    void default_constructor_uses_system_clock() {
        // Smoke-test: the no-arg constructor must not throw and must return a non-null status
        WorkingDayDecider decider = new WorkingDayDecider();

        FlowExecutionStatus status = decider.decide(jobExecution(), null);

        assertNotNull(status);
    }
}
