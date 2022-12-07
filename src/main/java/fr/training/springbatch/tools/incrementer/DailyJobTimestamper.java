package fr.training.springbatch.tools.incrementer;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersIncrementer;

/**
 * {@link JobParametersIncrementer} that provide a date job parameter
 */
public class DailyJobTimestamper implements JobParametersIncrementer {

    private static final Logger logger = LoggerFactory.getLogger(DailyJobTimestamper.class);

    private String parameterName = "currentDate";

    public DailyJobTimestamper() {

    }

    public DailyJobTimestamper(final String parameterName) {
        this.parameterName = parameterName;
    }

    @Override
    public JobParameters getNext(final JobParameters jobParameters) {
        final String today = LocalDate.now().toString();
        logger.info("Adding new {}  parameter with value {}", parameterName, today);
        return new JobParametersBuilder(jobParameters).addString(getParameterName(), today).toJobParameters();
    }

    public String getParameterName() {
        return parameterName;
    }
}