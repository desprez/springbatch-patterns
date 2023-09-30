package fr.training.springbatch.job.purge;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import fr.training.springbatch.app.job.AbstractJobConfiguration;
import fr.training.springbatch.tools.tasklet.RemoveSpringBatchHistoryTasklet;

/**
 * This job use the {@link RemoveSpringBatchHistoryTasklet} to remove the old entries in the Spring-batch metadatas tables.
 */
@Configuration
@ConditionalOnProperty(name = "spring.batch.job.names", havingValue = PurgeHistoryJobConfig.PURGE_HISTORY_JOB)
public class PurgeHistoryJobConfig extends AbstractJobConfiguration {

    protected static final String PURGE_HISTORY_JOB = "purgehistory-job";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Bean
    Job job(final JobRepository jobRepository, final PlatformTransactionManager transactionManager) {

        final Step purgeHistoryStep = new StepBuilder("purgehistorystep", jobRepository) //
                .tasklet(new RemoveSpringBatchHistoryTasklet() {
                    {
                        setHistoryRetentionMonth(1);
                        setJdbcTemplate(jdbcTemplate);
                    }
                }, transactionManager).build();

        return new JobBuilder(PURGE_HISTORY_JOB, jobRepository) //
                .start(purgeHistoryStep) //
                .build();
    }

}
