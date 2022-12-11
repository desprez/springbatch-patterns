package fr.training.springbatch.job.purge;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import fr.training.springbatch.app.job.AbstractJobConfiguration;
import fr.training.springbatch.tools.tasklet.RemoveSpringBatchHistoryTasklet;

/**
 * This job use the {@link RemoveSpringBatchHistoryTasklet} to remove the old entries in the Spring-batch metadatas tables.
 */
public class PurgeHistoryJobConfig extends AbstractJobConfiguration {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Bean
    Job job() {

        final Step purgeHistoryStep = stepBuilderFactory.get("purgehistorystep") //
                .tasklet(new RemoveSpringBatchHistoryTasklet() {
                    {
                        setHistoryRetentionMonth(1);
                        setJdbcTemplate(jdbcTemplate);
                    }
                }).build();

        return jobBuilderFactory.get("purgehistoryjob") //
                .start(purgeHistoryStep) //
                .build();
    }

}
