package fr.training.springbatch.job.history;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import fr.training.springbatch.app.job.AbstractJobConfiguration;
import fr.training.springbatch.tools.tasklet.RemoveSpringBatchHistoryTasklet;

/**
 * <b>Pattern #20</b> This job use the {@link RemoveSpringBatchHistoryTasklet} to remove the old entries in the Spring-batch metadatas tables.
 *
 * @author Desprez
 */
@Configuration
@ConditionalOnProperty(name = "spring.batch.job.names", havingValue = PruneHistoryJobConfig.PRUNE_HISTORY_JOB)
public class PruneHistoryJobConfig extends AbstractJobConfiguration {

    protected static final String PRUNE_HISTORY_JOB = "prunehistory-job";

    private static final int ONE_MONTH = 1;

    @Bean
    Job pruneHistoryJob(final JobRepository jobRepository, final PlatformTransactionManager transactionManager,
            final Step pruneHistoryStep) {

        return new JobBuilder(PRUNE_HISTORY_JOB, jobRepository)
                .start(pruneHistoryStep)
                .build();
    }

    @Bean
    Step pruneHistoryStep(final JobRepository jobRepository, final PlatformTransactionManager transactionManager,
            final Tasklet pruneHistoryTasket) {

        return new StepBuilder("prunehistory-step", jobRepository)
                .tasklet(pruneHistoryTasket, transactionManager)
                .build();
    }

    @Bean
    RemoveSpringBatchHistoryTasklet pruneHistoryTasket(final JdbcTemplate jdbcTemplate) {
        final RemoveSpringBatchHistoryTasklet tasklet = new RemoveSpringBatchHistoryTasklet();
        tasklet.setHistoryRetentionMonth(ONE_MONTH);
        tasklet.setJdbcTemplate(jdbcTemplate);
        return tasklet;
    }

}
