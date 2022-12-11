package fr.training.springbatch.job.businessdays;

import java.time.ZoneId;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import fr.training.springbatch.app.job.AbstractJobConfiguration;
import fr.training.springbatch.job.daily.DailyJobConfig;
import fr.training.springbatch.tools.incrementer.DailyJobTimestamper;

public class BusinessDaysJobConfig extends AbstractJobConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(DailyJobConfig.class);

    @Bean
    Job dailyjob(final Step dailyStep) {
        return jobBuilderFactory.get("businessdays-job") //
                .incrementer(new DailyJobTimestamper("processDate")) //
                .validator(new DefaultJobParametersValidator(new String[]{"processDate"}, new String[]{})) //
                .start(dailyStep) //
                .build();
    }

    @JobScope
    @Bean
    Step dailyStep(@Value("#{jobParameters['processDate']}") final Date processDate) {
        return stepBuilderFactory.get("businessdays-step") //
                .tasklet((contribution, chunkContext) -> {
                    logger.info("Do some process with the date '{}'", processDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                    return RepeatStatus.FINISHED;
                }) //
                .build();
    }
}
