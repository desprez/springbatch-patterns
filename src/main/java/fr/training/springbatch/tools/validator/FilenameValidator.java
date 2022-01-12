package fr.training.springbatch.tools.validator;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.JobParametersValidator;
import org.springframework.util.StringUtils;

public class FilenameValidator implements JobParametersValidator {

	@Override
	public void validate(final JobParameters parameters) throws JobParametersInvalidException {
		final String filename = parameters.getString("filename");

		if (!StringUtils.hasText(filename)) {
			throw new JobParametersInvalidException("Filename parameter is missing");
		} else if (!StringUtils.endsWithIgnoreCase(filename, "csv")) {
			throw new JobParametersInvalidException("Filename parameter does not use the csv file extension");
		}
	}

}
