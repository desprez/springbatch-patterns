package fr.training.springbatch.tools.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;

class EnumerationJobParametersValidatorTest {

    public enum Option {
        ONE, TWO, THREE
    }

    public static class OptionValidator extends EnumerationJobParametersValidator<Option> {
        public OptionValidator(final String parameterKey) {
            super(parameterKey, Option.class);
        }
    }

    private static final String PARAMETER_NAME = "flag";

    OptionValidator validator = new OptionValidator(PARAMETER_NAME);

    @Test
    void validation_with_allowed_value_jobparameter_should_pass() throws JobParametersInvalidException {
        // Given
        final JobParameters parameters = new JobParametersBuilder() //
                .addString(PARAMETER_NAME, "ONE") //
                .toJobParameters();

        // Then
        assertDoesNotThrow(() -> {
            // When
            validator.validate(parameters);
        });
    }

    @Test
    void validation_with_incorrect_value_jobparameter_should_fail() throws JobParametersInvalidException {
        // Given
        final JobParameters parameters = new JobParametersBuilder() //
                .addString(PARAMETER_NAME, "FOUR") //
                .toJobParameters();

        final Throwable exceptionThatWasThrown = assertThrows(JobParametersInvalidException.class, () -> {
            // When
            validator.validate(parameters);
        });

        // Then
        assertThat(exceptionThatWasThrown.getMessage()).isEqualTo("Invalid value input for parameter:flag : allowed values are [ONE, TWO, THREE]");
    }

}
