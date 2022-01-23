package fr.training.springbatch.job.daily;

import java.time.ZoneId;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import fr.training.springbatch.app.job.AbstractJobConfiguration;

/**
 * This job is configured to run once per day and prevent to not be launch
 * twice.
 */
public class DailyJobConfig extends AbstractJobConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(DailyJobConfig.class);

	@Bean
	public Job dailyjob(final Step dailyStep) {
		return jobBuilderFactory.get("daily-job") //
				// no incrementer here to ensure that job run only once per day
				.validator(new DefaultJobParametersValidator(new String[] { "processDate" }, new String[] {})) //
				.start(dailyStep) //
				.build();
	}

	@JobScope
	@Bean
	public Step dailyStep(@Value("#{jobParameters['processDate']}") final Date processDate) {
		return stepBuilderFactory.get("daily-step") //
				.tasklet(new Tasklet() {
					@Override
					public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext)
							throws Exception {
						logger.info("Do some process with the date '{}'",
								processDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
						return RepeatStatus.FINISHED;
					}
				}) //
				.build();
	}
}
