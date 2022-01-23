package fr.training.springbatch.tools.validator;

import java.util.Arrays;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.JobParametersValidator;

public class EnumerationJobParametersValidator<E extends Enum<E>> implements JobParametersValidator {

	private final String parameterKey;

	private final Class<E> enumType;

	public EnumerationJobParametersValidator(final String parameterKey, final Class<E> enumType) {
		this.parameterKey = parameterKey;
		this.enumType = enumType;
	}

	@Override
	public void validate(final JobParameters parameters) throws JobParametersInvalidException {
		parameters.getString(parameterKey);
		try {
			Enum.valueOf(enumType, parameters.getString(parameterKey));
		} catch (final IllegalArgumentException e) {
			throw new JobParametersInvalidException("Invalid value input for parameter:" + parameterKey
					+ " : allowed values are " + Arrays.toString(enumType.getEnumConstants()));
		}
	}
}
