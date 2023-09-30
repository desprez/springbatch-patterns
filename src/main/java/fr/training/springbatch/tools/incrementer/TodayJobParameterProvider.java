package fr.training.springbatch.tools.incrementer;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersIncrementer;

/**
 * {@link JobParametersIncrementer} that provide a currentDate job parameter if not provided.
 */
public class TodayJobParameterProvider implements JobParametersIncrementer {

    private static final Logger logger = LoggerFactory.getLogger(TodayJobParameterProvider.class);

    private String parameterName = "currentDate";

    public TodayJobParameterProvider() {

    }

    public TodayJobParameterProvider(final String parameterName) {
        this.parameterName = parameterName;
    }

    @Override
    public JobParameters getNext(final JobParameters jobParameters) {
        final LocalDate today = LocalDate.now();
        if (jobParameters == null || jobParameters.isEmpty()) {
            logger.info("Adding new {}  parameter with value {}", parameterName, today);
            return new JobParametersBuilder(jobParameters).addLocalDate(getParameterName(), today).toJobParameters();
        }
        return jobParameters;
    }

    public String getParameterName() {
        return parameterName;
    }
}