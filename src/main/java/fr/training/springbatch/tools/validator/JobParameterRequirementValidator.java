package fr.training.springbatch.tools.validator;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.JobParametersValidator;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * This {@link JobParametersValidator} accepts a requirements function that takes a `JobParameter` object as input and returns a `String` value indicating
 * whether the job parameters meet the requirements.
 *
 * If the requirements are not met, the decorator will throw an `IllegalArgumentException` with a message indicating that the job parameters do not meet the
 * requirements.
 */
public class JobParameterRequirementValidator implements JobParametersValidator {

    private final String parameterKey;

    private final ParameterRequirement<?> requirement;

    public JobParameterRequirementValidator(final String parameterKey, final ParameterRequirement<?> parameterRequirement) {
        this.parameterKey = parameterKey;
        this.requirement = parameterRequirement;
    }

    @Override
    public void validate(final @Nullable JobParameters jobParameters) throws JobParametersInvalidException {
        Assert.notNull(jobParameters, "jobParameters must be not null");
        final String violation = requirement.validate(jobParameters.getParameter(parameterKey));
        if (violation != null) {
            throw new JobParametersInvalidException(formatMessage(violation, parameterKey));
        }
    }

    private String formatMessage(final String violation, final String parameterName) {
        return "JobParameter ".concat(StringUtils.quote(parameterName)).concat(violation);
    }

}
