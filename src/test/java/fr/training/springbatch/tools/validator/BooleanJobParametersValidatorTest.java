package fr.training.springbatch.tools.validator;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;

class BooleanJobParametersValidatorTest {

	private static final String PARAMETER_NAME = "flag";
	BooleanJobParametersValidator validator = new BooleanJobParametersValidator(PARAMETER_NAME);

	@Test
	void validation_with_true_jobparameter_should_pass() throws JobParametersInvalidException {
		// Given
		final JobParameters parameters = new JobParametersBuilder() //
				.addString(PARAMETER_NAME, "True") //
				.toJobParameters();

		// Then
		Assertions.assertDoesNotThrow(() -> {
			// When
			validator.validate(parameters);
		});
	}

	@Test
	void validation_with_false_jobparameter_should_pass() throws JobParametersInvalidException {
		// Given
		final JobParameters parameters = new JobParametersBuilder() //
				.addString(PARAMETER_NAME, "fALSE") //
				.toJobParameters();

		// Then
		Assertions.assertDoesNotThrow(() -> {
			// When
			validator.validate(parameters);
		});
	}

	@Test
	void validation_with_incorrect_jobparameter_should_fail() throws JobParametersInvalidException {
		// Given
		final JobParameters parameters = new JobParametersBuilder() //
				.addString(PARAMETER_NAME, "thuth") //
				.toJobParameters();

		// Then
		final Throwable exceptionThatWasThrown = Assertions.assertThrows(JobParametersInvalidException.class, () -> {
			// When
			validator.validate(parameters);
		});
		assertThat(exceptionThatWasThrown.getMessage())
		.isEqualTo("Expect only 'true' or 'false' values for the parameter flag");
	}

}
