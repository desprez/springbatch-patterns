package fr.training.springbatch.tools.validator;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;

class DisplayUsageJobParametersValidatorTest {

	@Test
	void testValidateNull() {
		// Given
		final DisplayUsageJobParametersValidator validator = DisplayUsageJobParametersValidator.builder().build();

		final Throwable exceptionThatWasThrown = Assertions.assertThrows(JobParametersInvalidException.class, () -> {
			// When
			validator.validate(null);
		});

		// Then
		assertThat(exceptionThatWasThrown.getMessage()).isEqualTo("The JobParameters can not be null");
	}

	@Test
	void testValidateNoRequiredValues() {
		final DisplayUsageJobParametersValidator validator = DisplayUsageJobParametersValidator.builder().build();

		Assertions.assertDoesNotThrow(() -> {
			validator.validate(new JobParametersBuilder().addString("name", "foo").toJobParameters());
		});
	}

	@Test
	void testValidateRequiredValues() {
		final DisplayUsageJobParametersValidator validator = DisplayUsageJobParametersValidator.builder() //
				.requiredKeys(new String[] { "name", "value" }) //
				.build();

		Assertions.assertDoesNotThrow(() -> {
			validator.validate(
					new JobParametersBuilder().addString("name", "foo").addLong("value", 111L).toJobParameters());
		});
	}

	@Test
	void testValidateRequiredValuesMissing() {
		// Given
		final DisplayUsageJobParametersValidator validator = DisplayUsageJobParametersValidator.builder() //
				.requiredKeys(new String[] { "name", "value" }) //
				.build();

		final Throwable exceptionThatWasThrown = Assertions.assertThrows(JobParametersInvalidException.class, () -> {
			// When
			validator.validate(new JobParameters());
		});

		// Then
		assertThat(exceptionThatWasThrown.getMessage())
		.isEqualTo("The JobParameters do not contain required keys: [name, value]");
	}

	@Test
	void testValidateOptionalValues() {
		// Given
		final DisplayUsageJobParametersValidator validator = DisplayUsageJobParametersValidator.builder() //
				.optionalKeys(new String[] { "name", "value" }) //
				.build();

		// Then
		Assertions.assertDoesNotThrow(() -> {
			// When
			validator.validate(new JobParameters());
		});
	}

	@Test
	void testValidateOptionalWithImplicitRequiredKey() throws Exception {
		// Given
		final DisplayUsageJobParametersValidator validator = DisplayUsageJobParametersValidator.builder() //
				.optionalKeys(new String[] { "name", "value" }) //
				.build();

		final Throwable exceptionThatWasThrown = Assertions.assertThrows(JobParametersInvalidException.class, () -> {
			// When
			validator.validate(new JobParametersBuilder().addString("foo", "bar").toJobParameters());
		});
		// Then
		assertThat(exceptionThatWasThrown.getMessage())
		.isEqualTo("The JobParameters contains keys that are not explicitly optional or required: [foo]");
	}

	@Test
	void testValidateOptionalWithExplicitRequiredKey() throws Exception {
		// Given
		final DisplayUsageJobParametersValidator validator = DisplayUsageJobParametersValidator.builder() //
				.optionalKeys(new String[] { "name", "value" }) //
				.requiredKeys(new String[] { "foo" }) //
				.build();
		// Then
		Assertions.assertDoesNotThrow(() -> {
			// When
			validator.validate(new JobParametersBuilder().addString("foo", "bar").toJobParameters());
		});
	}

	@Test
	void testOptionalValuesAlsoRequired() throws Exception {
		// Given
		final DisplayUsageJobParametersValidator validator = DisplayUsageJobParametersValidator.builder() //
				.optionalKeys(new String[] { "name", "value" }) //
				.requiredKeys(new String[] { "foo", "value" }) //
				.build();

		// Then
		final Throwable exceptionThatWasThrown = Assertions.assertThrows(IllegalStateException.class, () -> {
			// When
			validator.afterPropertiesSet();
		});
		// Then
		assertThat(exceptionThatWasThrown.getMessage()).isEqualTo("Optional keys cannot be required: value");
	}

}