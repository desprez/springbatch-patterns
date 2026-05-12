package fr.training.springbatch.job.timestamp;

import fr.training.springbatch.job.BatchTestConfiguration;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.test.JobOperatorTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
@ActiveProfiles("test")
@SpringBatchTest
@SpringBootTest(classes = { BatchTestConfiguration.class, DeltaBetweenLastLaunchJobConfig.class }, properties = { "spring.batch.job.enabled=false",
        "spring.batch.job.names=monitoringJob" })
class DeltaBetweenLastLaunchJobConfigTest {

    @Autowired
    private JobOperatorTestUtils testUtils;

    @Autowired
    private Job job;

    @Test
    void launch_with_nominal_parameters_should_success() throws Exception {

        // Given
        final JobParameters jobParameters = new JobParametersBuilder()
                .addLong("run.id", 0L)
                .toJobParameters();
        testUtils.setJob(job);

        // When
        final JobExecution result = testUtils.startJob(jobParameters);

        // Then
        assertThat(result.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(result).isNotNull();
        assertThat(result.getExitStatus()).isNotNull();
        assertThat(result.getExitStatus().getExitCode()).isEqualTo(ExitStatus.COMPLETED.getExitCode());

        final var stepExecOpt = result.getStepExecutions().stream()
                .filter(e -> "monitoringStep".equals(e.getStepName())).findFirst();
        assertThat(stepExecOpt).isPresent();
        final var stepExec = stepExecOpt.get();

        assertThat((int) stepExec.getReadCount()).isEqualTo(11);
        assertThat((int) stepExec.getWriteCount()).isEqualTo(11);

    }

}
