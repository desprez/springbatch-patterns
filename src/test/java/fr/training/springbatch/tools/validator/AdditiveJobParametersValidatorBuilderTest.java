package fr.training.springbatch.tools.validator;

import static fr.training.springbatch.tools.validator.ParameterRequirement.expectedValue;
import static fr.training.springbatch.tools.validator.ParameterRequirement.gt;
import static fr.training.springbatch.tools.validator.ParameterRequirement.identifying;
import static fr.training.springbatch.tools.validator.ParameterRequirement.lt;
import static fr.training.springbatch.tools.validator.ParameterRequirement.positiveNumber;
import static fr.training.springbatch.tools.validator.ParameterRequirement.required;
import static fr.training.springbatch.tools.validator.ParameterRequirement.valueIn;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.JobParametersValidator;

class AdditiveJobParametersValidatorBuilderTest {

    private static final LocalDate TODAY = LocalDate.of(2023, 11, 18);
    private static final LocalDate DAY_BEFORE = TODAY.minusDays(1L);
    private static final LocalDate DAY_AFTER = TODAY.plusDays(1L);

    @Test
    void validation_with_multiple_jobparameters_met_requirements_should_pass() throws JobParametersInvalidException {
        // Given
        final JobParameters jobParameters = new JobParametersBuilder()
                .addString("StringParam", "one")
                .addDate("DateParam", new Date())
                .addLocalDate("LocalDateParam", LocalDate.now())
                .addLocalDateTime("LocalDateTimeParam", LocalDateTime.now())
                .addLocalTime("LocalTimeParam", LocalTime.now())
                .addLong("LongParam", 1L, true)
                .addDouble("DoubleParam", 1.1D, true)
                .addJobParameter("BooleanParam", true, Boolean.class)
                .toJobParameters();

        final JobParametersValidator validator = new AdditiveJobParametersValidatorBuilder()
                .addValidator(new JobParameterRequirementValidator("StringParam", required().and(valueIn("one", "two", "three"))))
                .addValidator(new JobParameterRequirementValidator("DateParam", required()))
                .addValidator(new JobParameterRequirementValidator("LocalDateParam", required()))
                .addValidator(new JobParameterRequirementValidator("LocalDateTimeParam", required()))
                .addValidator(new JobParameterRequirementValidator("LocalTimeParam", required()))
                .addValidator(new JobParameterRequirementValidator("LongParam", required().and(positiveNumber())))
                .addValidator(new JobParameterRequirementValidator("DoubleParam", required().and(gt(0))))
                .addValidator(new JobParameterRequirementValidator("BooleanParam", required().and(expectedValue(true))))
                .build();

        // Then
        assertDoesNotThrow(() -> {
            // When
            validator.validate(jobParameters);
        });
    }

    @Test
    void validation_with_multiple_jobparameters_not_met_requirements_should_fails() throws JobParametersInvalidException {
        // Given
        final JobParameters jobParameters = new JobParametersBuilder()
                .addString("StringParam", "four")
                .addDate("DateParam", new Date(), false)
                .addLocalDate("LocalDateParam", TODAY)
                .addLocalDateTime("LocalDateTimeParam", LocalDateTime.now())
                .addLocalTime("LocalTimeParam", LocalTime.now())
                .addLong("LongParam", -1L, true)
                .addDouble("DoubleParam", 1.1D, true)
                .addJobParameter("BooleanParam", false, Boolean.class)
                .toJobParameters();

        final JobParametersValidator validator = new AdditiveJobParametersValidatorBuilder()
                .addValidator(new JobParameterRequirementValidator("StringParam", required().and(valueIn("one", "two", "three"))))
                .addValidator(new JobParameterRequirementValidator("DateParam", required().and(identifying())))
                .addValidator(new JobParameterRequirementValidator("LocalDateParam", required()))
                .addValidator(new JobParameterRequirementValidator("LocalDateTimeParam", required()))
                .addValidator(new JobParameterRequirementValidator("LocalTimeParam", required()))
                .addValidator(new JobParameterRequirementValidator("LongParam", required().and(positiveNumber())))
                .addValidator(new JobParameterRequirementValidator("DoubleParam", required().and(lt(1))))
                .addValidator(new JobParameterRequirementValidator("BooleanParam", required().and(expectedValue(true))))
                .build();

        // Then
        final Throwable exceptionThatWasThrown = assertThrows(JobParametersInvalidException.class, () -> {
            // When
            validator.validate(jobParameters);
        });
        assertThat(exceptionThatWasThrown.getMessage())
                .isEqualTo("JobParameter 'StringParam' must have value in 'one, two, three' (current value is : 'four'),\n"
                        + "JobParameter 'DateParam' must be identifying,\n"
                        + "JobParameter 'LongParam' must be a positive number,\n"
                        + "JobParameter 'DoubleParam' has value '1.1' but required value is less than '1',\n"
                        + "JobParameter 'BooleanParam' must have expected 'true' value.");
    }

}
