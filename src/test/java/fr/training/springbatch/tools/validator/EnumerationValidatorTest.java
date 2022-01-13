package fr.training.springbatch.tools.validator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;

class EnumerationValidatorTest {

	public enum Option {
		ONE, TWO, THREE
	}

	public static class OptionValidator extends EnumerationValidator<Option> {
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
		Assertions.assertDoesNotThrow(() -> {
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

		// Then
		Assertions.assertThrows(JobParametersInvalidException.class, () -> {
			// When
			validator.validate(parameters);
		}, "");
	}

}
