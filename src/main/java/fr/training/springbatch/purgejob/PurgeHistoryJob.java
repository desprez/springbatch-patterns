package fr.training.springbatch.purgejob;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import fr.training.springbatch.common.AbstractJobConfiguration;
import fr.training.springbatch.tools.tasklet.RemoveSpringBatchHistoryTasklet;

@Configuration
public class PurgeHistoryJob extends AbstractJobConfiguration {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Bean
	public Job job() {

		final Step purgeHistoryStep = stepBuilderFactory.get("purgehistorystep") //
				.tasklet(new RemoveSpringBatchHistoryTasklet() {
					{
						setHistoricRetentionMonth(1);
						setJdbcTemplate(jdbcTemplate);
					}
				}).build();

		return jobBuilderFactory.get("purgehistoryjob") //
				.start(purgeHistoryStep) //
				.build();
	}

}
