package fr.training.springbatch.tools.chaos.tasklet;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Test upon BipolarTasklet class
 */
@ExtendWith(SpringExtension.class)
class BipolarTaskletTest {

    @Test
    void jobExecutionWithJavaConfig() throws Exception {
        // Given
        final ApplicationContext context = new AnnotationConfigApplicationContext(TestJobConfiguration.class);

        final JobLauncherTestUtils testUtils = context.getBean(JobLauncherTestUtils.class);

        // When launch job one time
        JobExecution execution = testUtils.launchJob();

        // then it fails
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.FAILED);

        // When launch job an another time
        execution = testUtils.launchJob();

        // then it pass
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    }

    @Configuration
    @EnableBatchProcessing
    public static class TestJobConfiguration {

        @Autowired
        public JobBuilderFactory jobBuilderFactory;

        @Autowired
        public StepBuilderFactory stepBuilderFactory;

        @Bean
        Step step() {
            return stepBuilderFactory.get("step1") //
                    .tasklet(bipolarTasklet()) //
                    .build();
        }

        @Bean
        Job job() {
            return jobBuilderFactory.get("job") //
                    .incrementer(new RunIdIncrementer()) //
                    .flow(step()) //
                    .end() //
                    .build();
        }

        @Bean
        BipolarTasklet bipolarTasklet() {
            return new BipolarTasklet();
        }

        @Bean
        JobLauncherTestUtils testUtils() {
            final JobLauncherTestUtils jobLauncherTestUtils = new JobLauncherTestUtils();
            jobLauncherTestUtils.setJob(job());
            return jobLauncherTestUtils;
        }
    }

}