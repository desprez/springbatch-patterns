package fr.training.springbatch.job.timestamp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
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
@SpringBootTest(classes = { BatchTestConfiguration.class, ProductionReportJobConfig.class }, properties = { "spring.batch.job.enabled=false",
        "spring.batch.job.names=monitoringJob" })
class ProductionReportJobTest {

    @Autowired
    private JobLauncherTestUtils singleJobLauncherTestUtils;

    @Test
    void launch_with_nominal_parameters_should_success() throws Exception {

        // Given
        final JobParameters jobParameters = new JobParametersBuilder()
                .addLong("run.id", 0L)
                .toJobParameters();
        // When
        final JobExecution result = this.singleJobLauncherTestUtils.launchJob(jobParameters);

        // Then
        assertThat(result.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(result).isNotNull();
        assertThat(result.getExitStatus()).isNotNull();
        assertThat(result.getExitStatus().getExitCode()).isEqualTo(ExitStatus.COMPLETED.getExitCode());

        final var stepExecOpt = result.getStepExecutions().stream()
                .filter(e -> "monitoringStep".equals(e.getStepName())).findFirst();
        assertThat(stepExecOpt).isPresent();
        final var stepExec = stepExecOpt.get();

        assertThat(stepExec.getReadCount()).isEqualTo(11);
        assertThat(stepExec.getWriteCount()).isEqualTo(11);

    }

}
