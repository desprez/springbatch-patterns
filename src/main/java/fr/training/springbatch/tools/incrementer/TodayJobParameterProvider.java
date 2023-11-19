package fr.training.springbatch.tools.incrementer;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.lang.Nullable;

/**
 * {@link JobParametersIncrementer} that provide a currentDate job parameter if not provided.
 */
public class TodayJobParameterProvider implements JobParametersIncrementer {

    private static final Logger logger = LoggerFactory.getLogger(TodayJobParameterProvider.class);

    private final String parameterName;

    public TodayJobParameterProvider() {
        this.parameterName = "currentDate";
    }

    public TodayJobParameterProvider(final String parameterName) {
        this.parameterName = parameterName;
    }

    @Override
    public JobParameters getNext(@Nullable final JobParameters lastExecJobParameters) {
        final LocalDate today = LocalDate.now();
        if (lastExecJobParameters == null || lastExecJobParameters.getLocalDate(parameterName) == null) {
            final JobParameters params = lastExecJobParameters == null ? new JobParameters() : lastExecJobParameters;
            logger.info("Adding new {}  parameter with value {}", parameterName, today);
            return new JobParametersBuilder(params).addLocalDate(parameterName, today).toJobParameters();
        }
        return lastExecJobParameters;
    }

}