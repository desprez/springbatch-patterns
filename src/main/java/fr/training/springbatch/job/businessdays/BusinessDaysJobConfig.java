package fr.training.springbatch.job.businessdays;

import java.time.ZoneId;
import java.util.Date;

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
import fr.training.springbatch.job.daily.DailyJobConfig;
import fr.training.springbatch.tools.incrementer.TodayJobParameterProvider;

@Configuration
@ConditionalOnProperty(name = "spring.batch.job.names", havingValue = BusinessDaysJobConfig.BUSINESSDAYS_JOB)
public class BusinessDaysJobConfig extends AbstractJobConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(DailyJobConfig.class);

    protected static final String BUSINESSDAYS_JOB = "businessdays-job";

    @Bean
    Job dailyjob(final Step dailyStep, final JobRepository jobRepository) {
        return new JobBuilder(BUSINESSDAYS_JOB, jobRepository) //
                .incrementer(new TodayJobParameterProvider("processDate")) //
                .validator(new DefaultJobParametersValidator(new String[] { "processDate" }, new String[] {})) //
                .start(dailyStep) //
                .build();
    }

    @JobScope
    @Bean
    Step dailyStep(final JobRepository jobRepository, final PlatformTransactionManager transactionManager,
            @Value("#{jobParameters['processDate']}") final Date processDate) {
        return new StepBuilder("businessdays-step", jobRepository) //
                .tasklet((contribution, chunkContext) -> {
                    logger.info("Do some process with the date '{}'", processDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                    return RepeatStatus.FINISHED;
                }, transactionManager) //
                .build();
    }
}
