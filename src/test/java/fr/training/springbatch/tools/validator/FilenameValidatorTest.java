package fr.training.springbatch.tools.validator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;

class FilenameValidatorTest {

	private final FilenameValidator validator = new FilenameValidator();

	@Test
	void validation_with_filename_jobparameter_should_pass() throws JobParametersInvalidException {
		// Given
		final JobParameters parameters = new JobParametersBuilder() //
				.addString("filename", "myfile.csv") //
				.toJobParameters();

		// Then
		Assertions.assertDoesNotThrow(() -> {
			// When
			validator.validate(parameters);
		});

	}

	@Test
	void validation_with_missing_filename_jobparameter_should_fail() throws JobParametersInvalidException {
		// Given
		final JobParameters parameters = new JobParametersBuilder() //
				.toJobParameters();

		// Then
		Assertions.assertThrows(JobParametersInvalidException.class, () -> {
			// When
			validator.validate(parameters);
		});
	}

	@Test
	void validation_with_missing_filename_extension_should_fail() throws JobParametersInvalidException {
		// Given
		final JobParameters parameters = new JobParametersBuilder() //
				.addString("filename", "myfile.doe") //
				.toJobParameters();

		// Then
		Assertions.assertThrows(JobParametersInvalidException.class, () -> {
			// When
			validator.validate(parameters);
		});
	}

}
