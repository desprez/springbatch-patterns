package fr.training.springbatch.job.extract;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import fr.training.springbatch.job.BatchTestConfiguration;

@ActiveProfiles("test")
@SpringBatchTest
@SpringBootTest(classes = { BatchTestConfiguration.class, SimpleExtractJobConfig.class }, properties = { "spring.batch.job.enabled=false",
        "spring.batch.job.names=simple-extract-job" })
class SimpleExtractJobTest {

    @Autowired
    private JobLauncherTestUtils testUtils;

    @Test
    void launch_SimpleExtractJob_nominal_should_success() throws Exception {
        // Given

        final JobParameters jobParameters = new JobParametersBuilder(testUtils.getUniqueJobParameters()).addString("output-dir", "target/output") //
                .toJobParameters();
        // When
        final JobExecution jobExec = testUtils.launchJob(jobParameters);
        // Then
        assertThat(jobExec.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    }

}