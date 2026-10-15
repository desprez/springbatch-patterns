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

class BusinessDayDeciderTest {

    // BusinessDay = Monday–Saturday, excluding French public holidays

    private static Clock clockFor(LocalDate date) {
        return Clock.fixed(date.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC);
    }

    private static JobExecution jobExecution() {
        return MetaDataInstanceFactory.createJobExecution();
    }

    @ParameterizedTest(name = "{1}")
    @CsvSource({
        "2024-03-11, Monday is a business day",
        "2024-03-15, Friday is a business day",
        "2024-03-16, Saturday is a business day (Mon-Sat rule)"
    })
    void decide_returns_CONTINUE_on_business_day(LocalDate date, String description) {
        BusinessDayDecider decider = new BusinessDayDecider(clockFor(date));

        FlowExecutionStatus status = decider.decide(jobExecution(), null);

        assertEquals("CONTINUE", status.getName());
    }

    @ParameterizedTest(name = "{1}")
    @CsvSource({
        "2024-03-17, Sunday is not a business day",
        "2024-05-01, Fete du travail (Wednesday) is a holiday",
        "2024-12-25, Noel (Wednesday) is a holiday"
    })
    void decide_returns_COMPLETED_on_non_business_day(LocalDate date, String description) {
        BusinessDayDecider decider = new BusinessDayDecider(clockFor(date));

        FlowExecutionStatus status = decider.decide(jobExecution(), null);

        assertEquals(FlowExecutionStatus.COMPLETED.getName(), status.getName());
    }

    @Test
    void default_constructor_uses_system_clock() {
        // Smoke-test: the no-arg constructor must not throw and must return a non-null status
        BusinessDayDecider decider = new BusinessDayDecider();

        FlowExecutionStatus status = decider.decide(jobExecution(), null);

        assertNotNull(status);
    }
}
