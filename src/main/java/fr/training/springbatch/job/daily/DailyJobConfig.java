package fr.training.springbatch.job.daily;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import fr.training.springbatch.app.job.AbstractJobConfiguration;
import fr.training.springbatch.tools.incrementer.DailyJobTimestamper;
import fr.training.springbatch.tools.listener.ElapsedTimeJobListener;

/**
 * This job is configured to run once per day and prevent to not be launch twice.
 */
public class DailyJobConfig extends AbstractJobConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(DailyJobConfig.class);

    @Bean
    Job dailyjob(final Step dailyStep) {
        return jobBuilderFactory.get("daily-job") //
                .incrementer(new DailyJobTimestamper("processDate")) //
                // .validator(new DefaultJobParametersValidator(new String[] { "processDate" }, new String[] {})) //
                .start(dailyStep) //
                .listener(new ElapsedTimeJobListener()) //
                .build();
    }

    @JobScope
    @Bean
    Step dailyStep(@Value("#{jobParameters['processDate']}") final String processDate) {
        return stepBuilderFactory.get("daily-step") //
                .tasklet((contribution, chunkContext) -> {
                    logger.info("Do some process with the date '{}'", processDate);
                    return RepeatStatus.FINISHED;
                }) //
                .build();
    }

}
