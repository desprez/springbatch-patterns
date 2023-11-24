package fr.training.springbatch.tools.validator;

import static fr.training.springbatch.tools.validator.ParameterRequirement.directoryExist;
import static fr.training.springbatch.tools.validator.ParameterRequirement.fileExist;
import static fr.training.springbatch.tools.validator.ParameterRequirement.fileReadable;
import static fr.training.springbatch.tools.validator.ParameterRequirement.fileWritable;
import static fr.training.springbatch.tools.validator.ParameterRequirement.identifying;
import static fr.training.springbatch.tools.validator.ParameterRequirement.optional;
import static fr.training.springbatch.tools.validator.ParameterRequirement.required;
import static fr.training.springbatch.tools.validator.ParameterRequirement.type;
import static fr.training.springbatch.tools.validator.ParameterRequirement.valueIn;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.JobParametersValidator;

class JobParameterRequirementValidatorTest {

    private static final String OUTPUT_FILE = "target/output/outputfile.csv";

    private static final String OUTPUT_DIR = "target/";

    private static final String INPUT_FILE = "src/main/resources/csv/customer.csv";

    @Test
    void validation_with_met_requiment_should_pass() {
        // Given
        final JobParameters jobParameters = new JobParametersBuilder()
                .addString("stringParam", "a value")
                .toJobParameters();
        final JobParametersValidator validator = new JobParameterRequirementValidator("stringParam",
                required().and(identifying()).and(type(String.class)));

        // Then
        assertDoesNotThrow(() -> {
            // When
            validator.validate(jobParameters);
        });
    }

    @Test
    void validation_with_not_met_requiment_should_fails() {
        // Given
        final JobParameters jobParameters = new JobParametersBuilder()
                .addString("stringParam", "a value")
                .toJobParameters();
        final JobParametersValidator validator = new JobParameterRequirementValidator("otherParam", required().and(identifying()));

        // When
        final JobParametersInvalidException exception = assertThrows(JobParametersInvalidException.class, () -> {
            validator.validate(jobParameters);
        });
        // Then
        assertThat(exception.getMessage()).isEqualTo("JobParameter 'otherParam' is required");
    }

    @Test
    void should_fail_with_optional_given_parameter_not_met() {
        // Given
        final JobParameters jobParameters = new JobParametersBuilder()
                .addString("otherParam", "a value")
                .toJobParameters();
        final JobParametersValidator validator = new JobParameterRequirementValidator("otherParam", optional().and(valueIn("one", "two", "three")));

        // When
        final JobParametersInvalidException exception = assertThrows(JobParametersInvalidException.class, () -> {
            validator.validate(jobParameters);
        });
        // Then
        assertThat(exception.getMessage()).isEqualTo("JobParameter 'otherParam' must have value in 'one, two, three' (current value is : 'a value')");
    }

    @Test
    void should_fail_with_missing_file_path_parameter() {
        // Given
        final JobParameters jobParameters = new JobParametersBuilder()
                .toJobParameters();

        final JobParametersValidator validator = new JobParameterRequirementValidator("output-file", required().and(fileExist().and(fileWritable())));

        // Then
        final Throwable exceptionThatWasThrown = assertThrows(JobParametersInvalidException.class, () -> {
            // When
            validator.validate(jobParameters);
        });
        assertThat(exceptionThatWasThrown.getMessage()).isEqualTo("JobParameter 'output-file' is required");
    }

    @Test
    void should_fail_with_not_existing_file() {
        // Given
        final JobParameters jobParameters = new JobParametersBuilder()
                .addString("input-file", "src/main/resources/csv/unknown.csv")
                .toJobParameters();

        final JobParametersValidator validator = new JobParameterRequirementValidator("input-file", fileExist());

        // Then
        final Throwable exceptionThatWasThrown = assertThrows(JobParametersInvalidException.class, () -> {
            // When
            validator.validate(jobParameters);
        });
        assertThat(exceptionThatWasThrown.getMessage()).isEqualTo("JobParameter 'input-file' with path [src/main/resources/csv/unknown.csv] does not exist");
    }

    @Test
    void should_pass_with_existing_directory_path_parameter() {
        // Given
        final JobParameters jobParameters = new JobParametersBuilder()
                .addString("output-directory", OUTPUT_DIR)
                .toJobParameters();

        final JobParametersValidator inputFileValidator = new JobParameterRequirementValidator("output-directory", directoryExist());

        assertDoesNotThrow(() -> {
            inputFileValidator.validate(jobParameters);
        });
    }

    @Test
    void should_pass_with_existing_readable_file_path_parameter() {
        // Given
        final JobParameters jobParameters = new JobParametersBuilder()
                .addString("input-file", INPUT_FILE)
                .addString("output-file", OUTPUT_FILE)
                .toJobParameters();

        final JobParametersValidator inputFileValidator = new JobParameterRequirementValidator("input-file", fileExist().and(fileReadable()));

        assertDoesNotThrow(() -> {
            inputFileValidator.validate(jobParameters);
        });
    }

    @Test
    void should_pass_with_existing_writable_file_path_parameter() {
        // Given
        final JobParameters jobParameters = new JobParametersBuilder()
                .addString("input-file", INPUT_FILE)
                .addString("output-file", OUTPUT_FILE)
                .toJobParameters();

        final JobParametersValidator outputFileValidator = new JobParameterRequirementValidator("output-file", fileExist().and(fileWritable()));

        assertDoesNotThrow(() -> {
            outputFileValidator.validate(jobParameters);
        });
    }

}
