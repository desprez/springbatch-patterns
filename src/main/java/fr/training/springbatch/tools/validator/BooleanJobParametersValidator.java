package fr.training.springbatch.tools.validator;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.JobParametersValidator;

public class BooleanJobParametersValidator implements JobParametersValidator {

    private final String parameterKey;

    public BooleanJobParametersValidator(final String parameterName) {
        parameterKey = parameterName;
    }

    @Override
    public void validate(final JobParameters parameters) throws JobParametersInvalidException {

        final String paramValue = parameters.getString(parameterKey);

        if (!("true".equalsIgnoreCase(paramValue) || "false".equalsIgnoreCase(paramValue))) {
            throw new JobParametersInvalidException("Expect only 'true' or 'false' values for the parameter " + parameterKey);
        }
    }

}
