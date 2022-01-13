package fr.training.springbatch.tools.validator;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.JobParametersValidator;

public class BooleanValidator implements JobParametersValidator {

	private final String parameterKey;

	public BooleanValidator(final String parameterName) {
		this.parameterKey = parameterName;
	}

	@Override
	public void validate(final JobParameters parameters) throws JobParametersInvalidException {

		final String paramValue = parameters.getString(parameterKey);

		if (!(paramValue.equalsIgnoreCase("true") || paramValue.equalsIgnoreCase("false"))) {
			throw new JobParametersInvalidException(
					"Expect only 'true' or 'false' values for the parameter " + parameterKey);
		}
	}

}
