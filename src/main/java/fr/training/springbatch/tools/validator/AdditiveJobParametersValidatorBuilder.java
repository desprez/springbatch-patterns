package fr.training.springbatch.tools.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.JobParametersValidator;

public class AdditiveJobParametersValidatorBuilder {

    private final List<JobParametersValidator> validators = new ArrayList<>();

    public AdditiveJobParametersValidatorBuilder addValidator(final JobParametersValidator validator) {
        validators.add(validator);
        return this;
    }

    public AdditiveJobParametersValidatorBuilder addValidators(final JobParametersValidator... validatorArray) {
        for (final JobParametersValidator validator : validatorArray) {
            addValidator(validator);
        }
        return this;
    }

    public JobParametersValidator build() {
        return parameters -> {

            final List<String> errors = new ArrayList<>();

            for (final JobParametersValidator jobParametersValidator : validators) {
                try {
                    jobParametersValidator.validate(parameters);
                } catch (final JobParametersInvalidException e) {
                    errors.add(e.getMessage());
                }
            }

            if (!errors.isEmpty()) {
                final String errorMessages = errors.stream().collect(Collectors.joining(",\n"));
                throw new JobParametersInvalidException(errorMessages.concat("."));
            }
        };
    }

}