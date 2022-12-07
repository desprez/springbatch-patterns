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
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import fr.training.springbatch.app.job.AbstractJobConfiguration;

public class ExploreJobConfig extends AbstractJobConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(ExploreJobConfig.class);

    @Autowired
    private JobExplorer jobExplorer;

    @Bean
    public Job dailyjob(final Step exploreStep) {
        return jobBuilderFactory.get("explore-job") //
                .incrementer(new RunIdIncrementer()) //
                .start(exploreStep) //
                .build();
    }

    @Bean
    public Step exploreStep(final Tasklet explorerTasklet) {
        return stepBuilderFactory.get("explore-step") //
                .tasklet(explorerTasklet) //
                .build();
    }

    @Bean
    public Tasklet explorerTasklet() {
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
