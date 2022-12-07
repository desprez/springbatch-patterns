package fr.training.springbatch.tools.validator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.JobParametersValidator;
import org.springframework.util.StringUtils;

public class RegExJobParametersValidator implements JobParametersValidator {

    private final String parameterKey;

    // Regex to check valid .
    private final String regex;

    @Override
    public void validate(final JobParameters parameters) throws JobParametersInvalidException {

        final String value = parameters.getString(parameterKey);
        if (!StringUtils.hasText(value)) {
            throw new JobParametersInvalidException(String.format("%s parameter is missing", parameterKey));
        }
        final Pattern p = Pattern.compile(regex);
        final Matcher m = p.matcher(value);

        if (!m.matches()) {
            throw new JobParametersInvalidException(String.format("%s parameter value '%s' not match regex %s", parameterKey, value, regex));
        }
    }

    public RegExJobParametersValidator(final String parameterKey, final String regex) {
        this.parameterKey = parameterKey;
        this.regex = regex;
    }

}
