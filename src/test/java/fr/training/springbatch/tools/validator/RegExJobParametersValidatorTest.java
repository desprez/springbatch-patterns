package fr.training.springbatch.tools.validator;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;

class RegExJobParametersValidatorTest {

	private static final String PARAMETER = "myparam";
	private static final String REGEX = "^[A-Za-z]\\w{5,29}$";

	@Test
	void validation_with_correct_jobparameter_should_pass() {
		// Given
		final RegExJobParametersValidator validator = new RegExJobParametersValidator(PARAMETER, REGEX);

		final JobParameters parameters = new JobParametersBuilder() //
				.addString(PARAMETER, "somevalue_21") //
				.toJobParameters();

		// Then
		Assertions.assertDoesNotThrow(() -> {
			// When
			validator.validate(parameters);
		});

	}

	@Test
	void validation_with_incorrect_jobparameter_should_fail() {
		// Given
		final RegExJobParametersValidator validator = new RegExJobParametersValidator(PARAMETER, REGEX);

		final JobParameters parameters = new JobParametersBuilder() //
				.addString(PARAMETER, "21_somevalue") //
				.toJobParameters();

		// Then
		final Throwable exceptionThatWasThrown = Assertions.assertThrows(JobParametersInvalidException.class, () -> {
			// When
			validator.validate(parameters);
		});

		assertThat(exceptionThatWasThrown.getMessage()).isEqualTo("myparam parameter value '21_somevalue' not match regex ^[A-Za-z]\\w{5,29}$");
	}
	@Test
	void validation_with_missing_jobparameter_should_fail() {
		// Given
		final RegExJobParametersValidator validator = new RegExJobParametersValidator(PARAMETER, REGEX);

		final JobParameters parameters = new JobParametersBuilder() //
				.toJobParameters();

		// Then
		final Throwable exceptionThatWasThrown = Assertions.assertThrows(JobParametersInvalidException.class, () -> {
			// When
			validator.validate(parameters);
		});

		assertThat(exceptionThatWasThrown.getMessage()).isEqualTo("myparam parameter is missing");
	}

}
