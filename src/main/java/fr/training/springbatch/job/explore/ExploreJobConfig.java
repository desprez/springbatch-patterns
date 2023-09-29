package fr.training.springbatch.job.explore;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import fr.training.springbatch.app.job.AbstractJobConfiguration;

@Configuration
@ConditionalOnProperty(name = "spring.batch.job.names", havingValue = ExploreJobConfig.EXPLORE_JOB)
public class ExploreJobConfig extends AbstractJobConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(ExploreJobConfig.class);

    protected static final String EXPLORE_JOB = "explore-job";

    @Autowired
    private JobExplorer jobExplorer;

    @Bean
    Job dailyjob(final Step exploreStep, final JobRepository jobRepository) {
        return new JobBuilder(EXPLORE_JOB, jobRepository) //
                .incrementer(new RunIdIncrementer()) //
                .start(exploreStep) //
                .build();
    }

    @Bean
    Step exploreStep(final JobRepository jobRepository, final PlatformTransactionManager transactionManager, final Tasklet explorerTasklet) {
        return new StepBuilder("explore-step", jobRepository) //
                .tasklet(explorerTasklet, transactionManager) //
                .build();
    }

    @Bean
    Tasklet explorerTasklet() {
        return new ExploringTasklet(jobExplorer);
    }

    public static class ExploringTasklet implements Tasklet {

        private final JobExplorer explorer;

        public ExploringTasklet(final JobExplorer explorer) {
            this.explorer = explorer;
        }

        @Override
        public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {
            final String jobName = chunkContext.getStepContext().getJobName();
            final List<JobInstance> instances = explorer.getJobInstances(jobName, 0, Integer.MAX_VALUE);

            logger.info("There are {} job instances for the job {}", instances.size(), jobName);
            logger.info("They have had the following results");
            logger.info("****************************************************************");

            for (final JobInstance instance : instances) {
                final List<JobExecution> jobExecutions = explorer.getJobExecutions(instance);
                logger.info("Instance {} had {} executions", instance.getInstanceId(), jobExecutions.size());

                for (final JobExecution jobExecution : jobExecutions) {
                    logger.info("\tExecution {} resulted in ExitStatus {}", jobExecution.getId(), jobExecution.getExitStatus());
                }
            }

            return RepeatStatus.FINISHED;
        }
    }

}
