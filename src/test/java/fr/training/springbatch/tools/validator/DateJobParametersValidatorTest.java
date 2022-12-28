package fr.training.springbatch.tools.validator;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;

class DateJobParametersValidatorTest {

    private static final LocalDate TODAY = LocalDate.now();
    private static final LocalDate YESTERDAY = TODAY.minusDays(1L);
    private static final LocalDate TOMORROW = TODAY.plusDays(1L);

    private static final String PARAMETER_NAME = "workdate";
    DateJobParametersValidator validator = new DateJobParametersValidator(PARAMETER_NAME);

    @Test
    void validation_with_past_jobparameter_allowed_should_pass() throws JobParametersInvalidException {
        // Given
        validator.setPastDateAllowed(true);
        validator.setFutureDateAllowed(false);

        final JobParameters parameters = new JobParametersBuilder() //
                .addDate(PARAMETER_NAME, Date.from(YESTERDAY.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant())) //
                .toJobParameters();

        // Then
        assertDoesNotThrow(() -> {
            // When
            validator.validate(parameters);
        });
    }

    @Test
    void validation_with_future_jobparameter_allowed_should_pass() throws JobParametersInvalidException {
        // Given
        validator.setPastDateAllowed(false);
        validator.setFutureDateAllowed(true);

        final JobParameters parameters = new JobParametersBuilder() //
                .addDate(PARAMETER_NAME, Date.from(TOMORROW.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant())) //
                .toJobParameters();

        // Then
        assertDoesNotThrow(() -> {
            // When
            validator.validate(parameters);
        });
    }
}
