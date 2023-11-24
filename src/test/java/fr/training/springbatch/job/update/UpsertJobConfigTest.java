package fr.training.springbatch.job.update;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import fr.training.springbatch.job.BatchTestConfiguration;

@ActiveProfiles("test")
@SpringBatchTest
@SpringBootTest(classes = { BatchTestConfiguration.class, UpsertJobConfig.class }, properties = { "spring.batch.job.enabled=false",
        "spring.batch.job.names=upsert-job" })
class UpsertJobConfigTest {

    @Autowired
    private JobLauncherTestUtils testUtils;

    @Test
    void launch_UpsertJob_nominal_should_success() throws Exception {
        // Given
        final JobParameters jobParameters = new JobParametersBuilder(testUtils.getUniqueJobParameters())
                .addString("input-file", "src/main/resources/csv/customer-upsert.csv")
                .toJobParameters();
        // When
        final JobExecution jobExec = testUtils.launchJob(jobParameters);
        // Then
        assertThat(jobExec.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        // And expected read / write counts
        final Optional<StepExecution> executionOpt = jobExec.getStepExecutions().stream().filter(e -> "upsert-step".equals(e.getStepName())).findFirst();
        assertThat(executionOpt.isPresent()).isTrue();
        final StepExecution stepExec = executionOpt.get();

        assertThat(stepExec.getReadCount()).isEqualTo(37);
        assertThat(stepExec.getWriteCount()).isEqualTo(37);
    }
}
