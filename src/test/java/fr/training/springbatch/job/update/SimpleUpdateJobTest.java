package fr.training.springbatch.job.update;

import fr.training.springbatch.job.BatchTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.test.JobOperatorTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBatchTest
@SpringBootTest(classes = { BatchTestConfiguration.class, SimpleUpdateJobConfig.class }, properties = { "spring.batch.job.enabled=false",
        "spring.batch.job.names=simple-update-job" })
class SimpleUpdateJobTest {

    private static final String REJECT_FILE_PATH = "target/output/reject-updates.csv";

    @Autowired
    private JobOperatorTestUtils testUtils;

    @Autowired
    private Job job;

    @Test
    void launch_SimpleUpdateJob_nominal_should_success() throws Exception {
        // Given
        final JobParameters jobParameters = new JobParametersBuilder(testUtils.getUniqueJobParameters())
                .addString("input-file", "src/main/resources/csv/customer-update.csv") //
                .addString("rejectfile", REJECT_FILE_PATH) //
                .toJobParameters();
        testUtils.setJob(job);

        // When
        final JobExecution execution = testUtils.startJob(jobParameters);

        // Then
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(new File(REJECT_FILE_PATH)).doesNotExist();

        // And expected read / write counts
        final Optional<StepExecution> executionOpt = execution.getStepExecutions().stream().filter(e -> "simple-update-step".equals(e.getStepName())).findFirst();
        assertThat(executionOpt).isPresent();
        final StepExecution stepExec = executionOpt.get();

        assertThat((int) stepExec.getReadCount()).isEqualTo(200);
        assertThat((int) stepExec.getWriteCount()).isEqualTo(200);
    }

}
