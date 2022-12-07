package fr.training.springbatch.tools.validator;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;

class FilenameJobParametersValidatorTest {

    private static final String PARAMETER = "filename";

    @Test
    void validation_with_filename_jobparameter_should_pass() {
        // Given
        final FilenameJobParametersValidator validator = new FilenameJobParametersValidator(PARAMETER, "");

        final JobParameters parameters = new JobParametersBuilder() //
                .addString(PARAMETER, "myfile.csv") //
                .toJobParameters();

        // Then
        Assertions.assertDoesNotThrow(() -> {
            // When
            validator.validate(parameters);
        });

    }

    @Test
    void validation_with_missing_filename_jobparameter_should_fail() {
        // Given
        final FilenameJobParametersValidator validator = new FilenameJobParametersValidator(PARAMETER, "");

        final JobParameters parameters = new JobParametersBuilder() //
                .toJobParameters();

        // Then
        final Throwable exceptionThatWasThrown = Assertions.assertThrows(JobParametersInvalidException.class, () -> {
            // When
            validator.validate(parameters);
        });

        assertThat(exceptionThatWasThrown.getMessage()).isEqualTo("filename parameter is missing");
    }

    @Test
    void validation_with_missing_filename_extension_should_fail() {
        // Given
        final FilenameJobParametersValidator validator = new FilenameJobParametersValidator(PARAMETER, "csv");

        final JobParameters parameters = new JobParametersBuilder() //
                .addString(PARAMETER, "myfile.doe") //
                .toJobParameters();

        // Then
        final Throwable exceptionThatWasThrown = Assertions.assertThrows(JobParametersInvalidException.class, () -> {
            // When
            validator.validate(parameters);
        });
        assertThat(exceptionThatWasThrown.getMessage()).isEqualTo("filename parameter does not use the csv file extension");
    }

}
