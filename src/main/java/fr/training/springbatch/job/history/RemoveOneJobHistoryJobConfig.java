package fr.training.springbatch.job.history;

import static fr.training.springbatch.tools.validator.ParameterRequirement.required;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import fr.training.springbatch.tools.tasklet.RemoveOneJobHistoryTasklet;
import fr.training.springbatch.tools.validator.JobParameterRequirementValidator;

/**
 * <b>Pattern #21</b> This job use the {@link RemoveOneJobHistoryTasklet} to remove the all entries in the Spring-batch metadatas tables for a given job name.
 *
 * @author Desprez
 */
@Configuration
@ConditionalOnProperty(name = "spring.batch.job.names", havingValue = RemoveOneJobHistoryJobConfig.REMOVE_JOB_HISTORY_JOB)
public class RemoveOneJobHistoryJobConfig {

    protected static final String REMOVE_JOB_HISTORY_JOB = "remove-job-history-job";

    @Bean
    Job removeJobHistoryJob(final JobRepository jobRepository, final PlatformTransactionManager transactionManager,
            final Step removeJobHistoryStep) {

        return new JobBuilder(REMOVE_JOB_HISTORY_JOB, jobRepository)
                .validator(new JobParameterRequirementValidator("job.name.to.remove", required()))
                .start(removeJobHistoryStep)
                .build();
    }

    @Bean
    Step removeJobHistoryStep(final JobRepository jobRepository, final PlatformTransactionManager transactionManager,
            final Tasklet removeOneJobHistoryTasklet) {

        return new StepBuilder("remove-job-history-step", jobRepository)
                .tasklet(removeOneJobHistoryTasklet, transactionManager)
                .build();
    }

    @StepScope // Mandatory for using jobParameters
    @Bean
    RemoveOneJobHistoryTasklet removeOneJobHistoryTasklet(final JdbcTemplate jdbcTemplate,
            @Value("#{jobParameters['job.name.to.remove']}") final String jobName) {

        final RemoveOneJobHistoryTasklet tasklet = new RemoveOneJobHistoryTasklet();
        tasklet.setJobName(jobName);
        tasklet.setJdbcTemplate(jdbcTemplate);
        return tasklet;
    }

}
