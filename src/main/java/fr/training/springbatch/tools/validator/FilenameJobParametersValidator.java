package fr.training.springbatch.tools.validator;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.JobParametersValidator;
import org.springframework.util.StringUtils;

public class FilenameJobParametersValidator implements JobParametersValidator {

	private String parameterKey = "filename";

	private String suffix = "";

	public FilenameJobParametersValidator(final String parameterKey, final String suffix) {
		this.parameterKey = parameterKey;
		this.suffix = suffix;
	}

	@Override
	public void validate(final JobParameters parameters) throws JobParametersInvalidException {

		final String filename = parameters.getString(parameterKey);

		if (!StringUtils.hasText(filename)) {
			throw new JobParametersInvalidException(String.format("%s parameter is missing", parameterKey));
		} else {
			if (StringUtils.hasText(suffix)) {
				if (!StringUtils.endsWithIgnoreCase(filename, suffix)) {
					throw new JobParametersInvalidException(
							String.format("%s parameter does not use the %s file extension", parameterKey, suffix));
				}
			}
		}
	}


}
