package fr.training.springbatch.job.daily;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import fr.training.springbatch.app.job.AbstractJobConfiguration;
import fr.training.springbatch.tools.incrementer.TodayJobParameterProvider;
import fr.training.springbatch.tools.listener.ElapsedTimeJobListener;

/**
 * This job is configured to run once per day and prevent to not be launch twice.
 */
@Configuration
@ConditionalOnProperty(name = "spring.batch.job.names", havingValue = DailyJobConfig.DAILY_JOB)
public class DailyJobConfig extends AbstractJobConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(DailyJobConfig.class);
    protected static final String DAILY_JOB = "daily-job";

    @Bean
    Job dailyjob(final Step dailyStep, final JobRepository jobRepository) {
        return new JobBuilder(DAILY_JOB, jobRepository) //
                .incrementer(new TodayJobParameterProvider("processDate")) //
                .validator(new DefaultJobParametersValidator(new String[] { "processDate" }, new String[] {})) //
                .start(dailyStep) //
                .listener(new ElapsedTimeJobListener()) //
                .build();
    }

    @JobScope
    @Bean
    Step dailyStep(final JobRepository jobRepository, final PlatformTransactionManager transactionManager,
            @Value("#{jobParameters['processDate']}") final LocalDate processDate) {

        return new StepBuilder("daily-step", jobRepository) //
                .tasklet((contribution, chunkContext) -> {
                    logger.info("Do some process with the date '{}'", processDate);
                    return RepeatStatus.FINISHED;
                }, transactionManager) //
                .build();
    }

}
