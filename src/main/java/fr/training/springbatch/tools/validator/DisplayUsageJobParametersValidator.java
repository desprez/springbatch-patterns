package fr.training.springbatch.tools.validator;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.JobParametersValidator;
import org.springframework.batch.core.job.DefaultJobParametersValidator;

public class DisplayUsageJobParametersValidator implements JobParametersValidator {

    private static final Logger log = LoggerFactory.getLogger(DisplayUsageJobParametersValidator.class);

    private final DefaultJobParametersValidator delegate;

    private String usageMessage = "Usage :";

    public DisplayUsageJobParametersValidator(final String[] requiredKeys, final String[] optionalKeys, final String usageMessage) {
        this.usageMessage = usageMessage.concat(generate(requiredKeys, optionalKeys));
        delegate = new DefaultJobParametersValidator(requiredKeys, optionalKeys);
    }

    @Override
    public void validate(final JobParameters parameters) throws JobParametersInvalidException {
        try {
            delegate.validate(parameters);
        } finally {
            log.error("Incorrect parameters.");
            log.info("Usage:");
            log.info(usageMessage);
        }
    }

    public void setUsageMessage(final String usageMessage) {
        this.usageMessage = usageMessage;
    }

    private String generate(final String[] requiredKeys, final String[] optionalKeys) {
        final StringBuilder sb = new StringBuilder();

        if (requiredKeys != null) {
            final List<String> requiredKeysList = Arrays.asList(requiredKeys);
            requiredKeysList.forEach(s -> sb.append(s).append(" (required)").append("\n"));
        }

        if (optionalKeys != null) {
            final List<String> optionalKeysList = Arrays.asList(optionalKeys);
            optionalKeysList.forEach(s -> sb.append(s).append(" (optional)").append("\n"));
        }

        return sb.toString();
    }

    /**
     * Private constructor to enforce Builder usage
     */
    private DisplayUsageJobParametersValidator(final Builder builder) {
        usageMessage = builder.usageMessage.concat(generate(builder.requiredKeys, builder.optionalKeys));
        delegate = new DefaultJobParametersValidator(builder.requiredKeys, builder.optionalKeys);
    }

    /**
     * Builder static assessor
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder pattern to ensure DisplayUsageJobParametersValidator is immutable.
     */
    public static class Builder {
        private String usageMessage = "";
        private String[] requiredKeys = new String[0];
        private String[] optionalKeys = new String[0];

        public Builder usageMessage(final String usageMessage) {
            this.usageMessage = usageMessage;
            return this;
        }

        public Builder requiredKeys(final String... requiredKeys) {
            this.requiredKeys = requiredKeys;
            return this;
        }

        public Builder optionalKeys(final String... optionalKeys) {
            this.optionalKeys = optionalKeys;
            return this;
        }

        public DisplayUsageJobParametersValidator build() {
            return new DisplayUsageJobParametersValidator(this);
        }
    }

    public void afterPropertiesSet() {
        delegate.afterPropertiesSet();
    }

}
