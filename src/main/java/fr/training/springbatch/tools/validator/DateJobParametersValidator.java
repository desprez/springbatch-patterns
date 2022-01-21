package fr.training.springbatch.tools.validator;

import java.util.Date;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.JobParametersValidator;
import org.springframework.beans.factory.InitializingBean;

public class DateJobParametersValidator implements JobParametersValidator, InitializingBean {

	private final String parameterKey;

	private boolean pastDateAllowed;

	private boolean futureDateAllowed;

	public DateJobParametersValidator(final String parameterName) {
		parameterKey = parameterName;
	}

	public DateJobParametersValidator(final String parameterKey, final boolean pastDateAllowed,
			final boolean futureDateAllowed) {
		this.parameterKey = parameterKey;
		this.pastDateAllowed = pastDateAllowed;
		this.futureDateAllowed = futureDateAllowed;
	}

	@Override
	public void validate(final JobParameters parameters) throws JobParametersInvalidException {
		final Date paramValue = parameters.getDate(parameterKey);

		final Date today = new Date();

		if (!pastDateAllowed) {
			if (paramValue.before(today)) {
				throw new JobParametersInvalidException(
						String.format("Past date value not allowed for %s parameter", parameterKey));
			}
		}
		if (!futureDateAllowed) {
			if (paramValue.after(today)) {
				throw new JobParametersInvalidException(
						String.format("Future date value not allowed for %s parameter", parameterKey));
			}
		}
	}

	public void setPastDateAllowed(final boolean pastDateAllowed) {
		this.pastDateAllowed = pastDateAllowed;
	}

	public void setFutureDateAllowed(final boolean futureDateAllowed) {
		this.futureDateAllowed = futureDateAllowed;
	}

	@Override
	public void afterPropertiesSet() throws Exception {

	}

}
